# Data Model: ai-paced brain-call liveness timeout

**Phase 1.** This feature has no persistent data store; its "model" is the small set of
**configuration knobs**, their **resolution order**, and the **run-health outcome** vocabulary.

## Configuration knobs

| Name | Type | Unit | Meaning |
|---|---|---|---|
| `brain_idle_timeout_s` | integer > 0 | seconds | Max silence (no new stdout) before the brain call is killed. **Primary** control. |
| `brain_hardcap_s` | integer > 0 | seconds | Max total wall-clock for one brain call, regardless of activity. **Backstop**. Must be ≥ idle timeout. |

### Resolution order (highest wins)

1. **Environment** — `AI_PACED_BRAIN_IDLE_TIMEOUT`, `AI_PACED_BRAIN_HARDCAP` (exported by the adapter).
2. **`run-modes.yml`** — `modes.ai-paced.brain_idle_timeout_s` / `brain_hardcap_s` (neutral default).
3. **Hard-coded floor** in the harness (last-resort so the loop can never hang even if config is
   missing/garbled): idle 300 s, hardcap 1800 s.

### Values by source

| Source | idle | hardcap |
|---|---|---|
| `adapters/claude-code` (env) | 120 | 900 |
| `adapters/opencode` (env) | 420 | 3600 |
| `run-modes.yml` (default) | 300 | 1800 |
| harness floor | 300 | 1800 |

### Validation rules

- Both must parse as positive integers; a non-integer/≤0 value is ignored and the next source in the
  order is used (never abort — the loop must not hang on bad config).
- `hardcap ≥ idle`; if a resolved pair violates this, the harness raises `hardcap` to `idle` and warns.

## Run-health outcome (extends spec 006 vocabulary)

`PROVENANCE_OUTCOME` enum in `scripts/lib/run-metrics.sh` — **one value added**:

| Value | When | Status |
|---|---|---|
| `ok` | plan complete, gate green | existing |
| `error` | brain returned failures / gate red at stop | existing |
| `cancelled` | escalated to a human / no tasks | existing |
| **`timeout`** | **run stopped because the brain stalled (hard-cap hit, or idle-timeout retries exhausted)** | **NEW** |

- `timeout` is content-free and numeric-adjacent like the rest (spec 006 invariant): only the enum
  crosses to git, never prompt/response content.
- `scripts/metrics.sh` counts `timeout` under its own label (not folded into `error`).

## State transitions (one brain call)

```text
start ─▶ [brain streaming] ──(finishes)──▶ return code N        → normal accounting (attempt ok/fail)
                │
                ├─(no stdout for idle_timeout)─▶ kill pgroup ──▶ return 124  → failed attempt (idle)
                │
                └─(total time ≥ hardcap)───────▶ kill pgroup ──▶ return 124  → failed attempt (hardcap)

per-task: failed attempts accumulate → fix_iter > max_fix_iterations → loop stops (on_failure)
run-level: if the stop was caused by stalls → PROVENANCE_OUTCOME = timeout
```

## Relationships

- **Reuses** spec 006 run-metrics sidecar (`.git/mirabilis/run-metrics.env`) and its consume-once
  hook — no schema change, one new enum value.
- **Reuses** the existing `max_fix_iterations` bound (ADR-0009) — a timeout is just a failed attempt.
- **Feeds** `scripts/metrics.sh` aggregation (spec 006) with the new labelled outcome.
