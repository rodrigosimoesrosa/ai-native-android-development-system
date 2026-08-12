# Implementation Plan: ai-paced brain-call liveness timeout

**Branch**: `007-ai-paced-brain-timeout` | **Date**: 2026-08-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/007-ai-paced-brain-timeout/spec.md`

## Summary

Harden the tool-neutral ai-paced harness (`scripts/ai-paced-run.sh`) so a **hung brain call can
never block the loop**. Wrap every brain invocation with a **progress-based idle-timeout** (kill the
brain if its stdout goes silent longer than a threshold) plus an absolute **hard-cap** on total
wall-clock, treat a timeout as a **failed fix attempt** (so the existing `max_fix_iterations` bound
takes over), and record a distinct `timeout` run-health outcome. Thresholds are supplied **per
adapter** via the environment so the neutral core stays brain-agnostic (fast cloud vs slow local);
`run-modes.yml` carries conservative neutral defaults.

Technical approach: reuse the **watchdog pattern already proven in operations** — run the brain with
its stdout tee'd to a log, and a background monitor that kills the brain's **process group** when the
log's mtime is stale beyond the idle threshold; bound total time with `timeout(1)` (coreutils) or an
equivalent alarm. No new dependency; only shell scripts, `run-modes.yml`, and the run-metrics
vocabulary change.

## Technical Context

**Language/Version**: Bash (POSIX-ish, macOS `/bin/bash` 3.2 **and** Linux bash ≥ 4 — CI parity).

**Primary Dependencies**: coreutils (`timeout`, `stat`, `date`, `kill`), git. No new third-party
dependency (Constitution gate: dependency-add **not** triggered). `timeout(1)` is GNU coreutils on
Linux; on macOS it may be absent — a pure-bash watchdog fallback is required (see research.md D1).

**Storage**: N/A. Threshold config lives in `run-modes.yml` (defaults) + environment (adapter
overrides). Run-health continues to flow through the `.git/mirabilis/run-metrics.env` sidecar
(spec 006) — one new enum value, no schema change.

**Testing**: shell tests in `scripts/tests/` (the existing pattern — `test_hook_trailers.sh`,
`test_consume_once.sh`, `test_metrics_agg.sh`). Tests drive a **fake brain** (a small script that
sleeps/streams on demand) so they are deterministic and need no real LLM.

**Target Platform**: developer machines + CI (macOS + Linux). The harness must behave identically on
both.

**Project Type**: tool-neutral SDD harness (shell) — the neutral core + tool adapters (ADR-0008).

**Performance Goals**: zero added latency for a healthy brain that completes before either threshold
(SC-003); termination of a stalled brain within one idle-interval of going silent (SC-001).

**Constraints**: neutral core must never hard-code a brain-specific latency (FR-004); the four human
gates and the verification-gate semantics are unchanged (FR-009); no orphaned brain processes after
termination (FR-010); works with macOS bash 3.2.

**Scale/Scope**: one wrapper around the single brain-invocation site in `ai-paced-run.sh`; two
adapter files supply thresholds; one `run-modes.yml` block; one enum value + its validation/agg; a
handful of shell tests. Small, surgical.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Gate | Status | Notes |
|---|---|---|
| I. Specs are source of truth | ✅ | Approved `spec.md` (007) with FR/SC drives this plan. |
| II. Small, verifiable units + SOLID | ✅ | One wrapper function around the brain call; thresholds injected (dependency inversion — the core depends on an env contract, adapters supply values). |
| III. Tests as executable spec (NON-NEGOTIABLE) | ✅ | Liveness behavior expressed as deterministic shell tests driving a fake stalling/streaming brain. Test-first. |
| IV. Knowledge in git + provenance | ✅ | All in versioned shell + `run-modes.yml`; the new `timeout` outcome is git-recorded run-health. |
| V. Neutral core, tools as adapters (NON-NEGOTIABLE) | ✅ | The mechanism is in the neutral harness; the **values** are adapter-supplied via env — the core never learns which brain runs. |
| Gate: dependency-add | ✅ Not triggered | Uses coreutils/bash only; a pure-bash fallback covers macOS lacking `timeout`. |
| Gate: architecture-change | ✅ Not triggered | Hardens ADR-0009's ai-paced on_failure/bounded-retry contract; contradicts no ADR. |
| ADR-0014 (outcome vocabulary) | ⚠️ Flag | Adds `timeout` to the run-health outcome family that already defers `refusal`/`cutoff`. **Human review** at merge: minor ADR-0014 extension vs its own ADR — not self-approved. |

**Result**: PASS — no unjustified violations; Complexity Tracking not required. One item flagged for
human judgment (the `timeout` outcome's ADR home).

## Project Structure

### Documentation (this feature)

```text
specs/007-ai-paced-brain-timeout/
├── plan.md              # This file
├── research.md          # Phase 0 — decisions (watchdog vs timeout(1), config surface, cleanup)
├── data-model.md        # Phase 1 — config knobs, defaults, the timeout outcome
├── quickstart.md        # Phase 1 — how to validate (fake stalling/streaming brain)
├── contracts/
│   └── harness-liveness.md   # Phase 1 — env contract (core↔adapter), run-modes keys, outcome vocab
└── tasks.md             # Phase 2 — created by /speckit-tasks (NOT here)
```

### Source Code (repository root)

Existing tool-neutral harness + adapters (ADR-0008); this feature edits, it does not add a module.

```text
scripts/
├── ai-paced-run.sh              # EDIT: wrap the brain call (line ~110) with the liveness watchdog;
│                                #       count timeout as a failed attempt; set outcome=timeout
├── lib/
│   ├── run-metrics.sh           # EDIT: add `timeout` to the valid PROVENANCE_OUTCOME enum
│   └── brain-watchdog.sh        # NEW: reusable idle-timeout + hard-cap wrapper (sourced by harness)
├── metrics.sh                   # EDIT: aggregate/label the `timeout` outcome
└── tests/
    ├── test_brain_idle_timeout.sh   # NEW: stalled fake brain → killed at idle, counted as attempt
    ├── test_brain_hardcap.sh        # NEW: streaming-forever fake brain → killed at hard-cap
    ├── test_brain_healthy.sh        # NEW: fast fake brain → no premature kill, unchanged behavior
    └── fixtures/fake-brain.sh       # NEW: parametrized fake brain (sleep / stream / finish)

run-modes.yml                    # EDIT: ai-paced block gains brain_idle_timeout_s + brain_hardcap_s
adapters/claude-code/run-ai-paced.sh   # EDIT: export tight thresholds (cloud, fast)
adapters/opencode/run-ai-paced.sh      # EDIT: export loose thresholds (local, slow)
```

**Structure Decision**: Keep the mechanism in a new `scripts/lib/brain-watchdog.sh` (sourced by the
harness) so it is unit-testable in isolation and reusable, while the *policy* (thresholds) lives in
`run-modes.yml` defaults + adapter env overrides. This honors ADR-0001/0008: the neutral core owns
the mechanism, adapters own the tool-specific values.

## Complexity Tracking

> Not required — Constitution Check passed with no unjustified violations.
