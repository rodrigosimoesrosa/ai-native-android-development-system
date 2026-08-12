# Implementation Plan: Per-Commit Run Metrics (Tokens + Run-Health Provenance)

**Branch**: `006-observability-run-metrics` | **Date**: 2026-08-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-observability-run-metrics/spec.md`

## Summary

Extend the existing git-native provenance plumbing (ADR-0010) so every agent-produced commit carries
**content-free run metrics**: tokens consumed and run-health (wall-time, retries, errors, outcome).
The signals reach the commit through a **session sidecar** written inside `.git/` — because the agent
and the harness do **not** commit (commits happen later / by a human, in a different shell), so
environment variables alone cannot carry the data to commit time. The `prepare-commit-msg` hook
**consumes and deletes** the sidecar on the first commit after a session — which cleanly gives
"one session → one commit" (no double counting) for free. `scripts/metrics.sh` aggregates the new
trailers per spec/agent/model. Cost/USD, traces, logs, and evals are **out of scope** (later phases).

Capture is split along the constitution's neutral-core/adapter boundary: the **neutral harness**
(`scripts/ai-paced-run.sh`) writes **run-health** (it already tracks iterations, gate pass/fail, and
can time itself); each **tool adapter** writes **tokens** (only it can read its model's usage).

## Technical Context

**Language/Version**: Bash (POSIX-ish, matches existing `scripts/*.sh`, `githooks/*`).

**Primary Dependencies**: git only. **No new third-party dependency** (tests are plain bash — no bats).

**Storage**: git commit trailers (durable); a transient session sidecar file inside `.git/`
(never tracked, never in the working tree).

**Testing**: dependency-free bash test scripts under `scripts/tests/`, run on throwaway temp git
repos; wired into CI as a read-only job.

**Target Platform**: developer machines + GitHub Actions CI (the existing `metrics` job).

**Project Type**: meta-system tooling (repo scripts/hooks/adapters), not app code.

**Performance Goals**: `metrics.sh` stays read-only and completes in < 5s on this repo (SC-004).

**Constraints**: content-free (no prompt/response ever in git — FR-007); `-` ≠ `0` (FR-004/005);
idempotent stamping (FR-008); respects adapter boundary (FR-014); no network in metrics (FR-013).

**Scale/Scope**: ~5 files touched + a small test suite. No app modules involved.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Gate | Status | Notes |
|---|---|---|
| I. Specs are source of truth | ✅ | Approved spec 006 drives this plan. |
| II. Small, verifiable units + SOLID | ✅ | ~5 focused files; clear contract between sidecar-writer (harness/adapter) and sidecar-consumer (hook). |
| III. Tests as executable spec (NON-NEGOTIABLE) | ✅ | Behavior expressed as **dependency-free bash tests** (hook consume/omit/idempotent, session→one-commit, metrics skip-`-`). Test-first. |
| IV. Knowledge in git + provenance | ✅ | Metrics live as commit trailers; sidecar is transient and `.git`-local; no second tracked source of truth. |
| V. Neutral core, tools as adapters | ✅ | **Tokens** captured in the tool adapter; **run-health + mechanism** stay tool-neutral. `check-adapter-boundary.sh` must stay green. |
| Governing: ADR-0010 (provenance/metrics) | ✅ | Extends its hook + metrics.sh; does not replace them. |
| Governing: ADR-0013 / ADR-0014 §3 | ✅ | Implements the token + run-health aggregate contract (cost deferred per spec). |
| Governing: ADR-0012 (adapter-scoped) | ✅ | Only content-free aggregates cross into git; capture stays adapter/harness-side. |
| Gate: dependency-add | ✅ Not triggered | git + bash only; no catalog/pkg change. |
| Gate: architecture-change | ✅ Not triggered | Extends ADR-0010/0009 plumbing as ADR-0013/0014 intend; no ADR contradicted. Normal human review at merge. |

**Result**: PASS — no violations; Complexity Tracking not required.

## Project Structure

### Documentation (this feature)

```text
specs/006-observability-run-metrics/
├── plan.md              # This file
├── research.md          # Phase 0 — decisions (sidecar channel, capture split, testing)
├── data-model.md        # Phase 1 — trailer schema, sidecar format, aggregated view
├── quickstart.md        # Phase 1 — how to validate end-to-end
├── contracts/
│   ├── trailer-schema.md    # commit-trailer keys + value semantics (0 vs -)
│   ├── sidecar-format.md    # .git-local session file: format + lifecycle
│   └── metrics-cli.md       # scripts/metrics.sh output contract
└── tasks.md             # Phase 2 — /speckit-tasks (NOT created here)
```

### Source Code (repository root)

Files created/extended (all meta-system tooling; no app modules):

```text
githooks/
└── prepare-commit-msg          # EXTEND: read run-metric env/sidecar, stamp present keys, delete sidecar (consume-once)

scripts/
├── ai-paced-run.sh             # EXTEND: time the run; on exit write run-health (latency/retries/errors/outcome) to the sidecar
├── metrics.sh                  # EXTEND: aggregate Provenance-Tokens/Latency-Ms/Retries/Errors/Outcome; skip `-`; tokenizer caveat
├── lib/run-metrics.sh          # NEW (neutral): shared helpers — sidecar path, append KEY=VALUE, safe read
└── tests/
    ├── run.sh                  # NEW: dependency-free test runner (temp git repos)
    ├── test_hook_trailers.sh   # NEW: hook stamps present keys, omits unset (`-`), idempotent, consume-once
    └── test_metrics_agg.sh     # NEW: metrics skips `-`, sums per spec/agent/model, tokenizer label

adapters/
├── claude-code/run-ai-paced.sh # EXTEND: parse per-call token usage, accumulate into the sidecar
└── opencode/run-ai-paced.sh    # EXTEND: parse local-model token counts, accumulate (omit if unavailable)
```

Docs touched: `methods/record-provenance.md` (document the new keys); CI `metrics` job already runs
`metrics.sh` (no change needed) — an optional `scripts-tests` CI job runs `scripts/tests/run.sh`.

**Structure Decision**: Reuse the ADR-0010 mechanism; add a `.git`-local **session sidecar** as the
capture→commit bridge and a neutral `scripts/lib/run-metrics.sh` shared by harness, adapters, and the
hook. Tokens live in adapters (Principle V); run-health + mechanism stay neutral. Rationale in
[research.md](research.md).

## Complexity Tracking

> Not required — Constitution Check passed with no violations.
