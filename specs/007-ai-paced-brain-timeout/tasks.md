---
description: "Task list for ai-paced brain-call liveness timeout (007)"
---

# Tasks: ai-paced brain-call liveness timeout

**Input**: Design documents from `/specs/007-ai-paced-brain-timeout/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/harness-liveness.md](contracts/harness-liveness.md)

**Governing decisions (ADRs)**: Implement WITHIN these — a conflicting task is a human gate (stop
and escalate):
- [ADR-0001](../../decisions/ADR-0001-build-on-existing-tools-neutral-core.md) / [ADR-0008](../../decisions/ADR-0008-methods-and-adapters-layout.md) — neutral core owns the mechanism; adapters own the values.
- [ADR-0009](../../decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md) — this hardens ai-paced's on_failure/bounded-retry contract.
- [ADR-0014](../../decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md) — the new `timeout` outcome extends its vocabulary → **T021 escalates its ADR home to a human**.

**Tests**: Included — Principle III (NON-NEGOTIABLE). Deterministic shell tests drive a **fake brain**
(no real LLM). Write test tasks FIRST and ensure they FAIL before implementing.

**Scope**: edits the tool-neutral harness + adapters (ADR-0008); no new module, no new dependency
(coreutils/bash only). No change to the verification gate or the four human gates (FR-009).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependency on incomplete tasks)
- **[Story]**: US1–US3 map to spec.md user stories

---

## Phase 1: Setup (Shared Infrastructure)

- [x] T001 [P] Create `scripts/tests/fixtures/fake-brain.sh` — a parametrized fake brain via `MODE`: `finish` (prints, exit 0), `stall` (prints once then sleeps forever), `runaway` (prints every 1s forever), `fail` (prints, exit 3); spawns a child sleep so process-group kill is exercised (quickstart.md; contracts §3)

---

## Phase 2: Foundational (Blocking Prerequisites)

**⚠️ CRITICAL**: blocks every user story — the neutral defaults every threshold-resolution path reads.

- [x] T002 Add neutral defaults to `run-modes.yml` under `modes.ai-paced`: `brain_idle_timeout_s: 300` and `brain_hardcap_s: 1800`, parseable by the same `awk` approach used for `max_fix_iterations` (contracts §2; data-model resolution order)

**Checkpoint**: `run-modes.yml` carries the neutral defaults; fake brain available for tests.

---

## Phase 3: User Story 1 - The harness never hangs on a stuck brain (Priority: P1) 🎯 MVP

**Goal**: A hung brain call is detected, killed, counted as a failed attempt, and folded into the
existing bounded-retry — the loop always stops and reports, never freezes.

**Independent Test**: drive `ai-paced-run.sh` with a `stall` fake brain and tiny thresholds; it exits
via on_failure within a bounded multiple of the thresholds, never blocks.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T003 [P] [US1] `scripts/tests/test_brain_idle_timeout.sh` — `stall` brain with idle=2s is killed at ~2s, wrapper returns 124, and NO child sleep survives (FR-001, FR-010, SC-001, SC-006)
- [x] T004 [P] [US1] `scripts/tests/test_brain_hardcap.sh` — `runaway` brain with hardcap=4s is killed at ~4s, wrapper returns 124 (FR-002)
- [x] T005 [US1] `scripts/tests/test_harness_no_hang.sh` — integration: `ai-paced-run.sh` with a one-line `tasks.md`, a `stall` brain, `AI_PACED_BRAIN_IDLE_TIMEOUT=2`/`HARDCAP=6`, wrapped in an outer `timeout 60`; assert the harness stops itself (on_failure) and the outer `timeout` is NOT what killed it (SC-001)

### Implementation

- [x] T006 [US1] Create `scripts/lib/brain-watchdog.sh` with `run_brain_with_liveness <idle> <hardcap> <cmd...>`: run the cmd in its own process group, tee stdout through unchanged, pure-bash idle watchdog (probe-and-pick `stat -f %m` vs `stat -c %Y`), hard-cap via `timeout(1)` when present else a bash-alarm fallback, kill the whole process group (TERM then KILL) on either limit → return 124 (FR-001, FR-002, FR-010; research D1/D2/D6; contracts §3)
- [x] T007 [US1] Wire into `scripts/ai-paced-run.sh`: replace the direct `bash -c "$AI_PACED_AGENT_CMD"` (~line 110) with `run_brain_with_liveness "$idle" "$hardcap" bash -c "$AI_PACED_AGENT_CMD"`; on return 124 log the stall and `continue` (so it counts as a failed `fix_iter` attempt) (FR-003; contracts §4)
- [x] T008 [US1] In `scripts/ai-paced-run.sh`, resolve `idle`/`hardcap` in order env → `run-modes.yml` → hard floor (300/1800); ignore non-integer/≤0 values (fall through, never abort) and raise `hardcap` to `idle` if `hardcap < idle` (FR-004, FR-005; data-model validation)
- [x] T009 [US1] Tune the wrapper until T003–T005 are GREEN; confirm zero orphaned processes after a kill (SC-006)

**Checkpoint**: MVP — the harness provably cannot hang; a stalled brain is bounded and reported.

---

## Phase 4: User Story 2 - Slow-but-healthy brains are not falsely killed (Priority: P1)

**Goal**: A slow-but-streaming brain runs uninterrupted; thresholds come per-adapter (cloud tight,
local loose) so the neutral core never hard-codes a latency profile.

**Independent Test**: a brain that prints just under the idle interval over a long run is never
killed; switching adapters changes the effective thresholds with no edit to `ai-paced-run.sh`.

**Depends on**: Phase 3 (the wrapper + resolution).

### Tests (write FIRST, must FAIL) ⚠️

- [x] T010 [P] [US2] `scripts/tests/test_brain_healthy.sh` — (a) `finish` brain returns 0 with no kill and no measurable added latency vs running it directly (SC-003); (b) a slow brain printing every `idle-1`s over several intervals is never killed (SC-002)

### Implementation

- [x] T011 [P] [US2] Export tight thresholds in `adapters/claude-code/run-ai-paced.sh`: `AI_PACED_BRAIN_IDLE_TIMEOUT=120`, `AI_PACED_BRAIN_HARDCAP=900` (cloud/fast) (FR-004; research D3)
- [x] T012 [P] [US2] Export loose thresholds in `adapters/opencode/run-ai-paced.sh`: `AI_PACED_BRAIN_IDLE_TIMEOUT=420`, `AI_PACED_BRAIN_HARDCAP=3600` (local/slow) (FR-004; research D3)
- [x] T013 [US2] Verify resolution: adapter env overrides the `run-modes.yml` default, and with no adapter value the default still bounds the run — with NO change to `ai-paced-run.sh` beyond reading env (SC-004); make T010 GREEN

**Checkpoint**: healthy slow brains run to completion; thresholds are adapter-scoped.

---

## Phase 5: User Story 3 - A stalled run is legible in the ledger (Priority: P2)

**Goal**: A stall-terminated run records a distinct `timeout` outcome, separate from `error`.

**Independent Test**: after a stall-terminated run, `Provenance-Outcome: timeout` is on the sidecar/
commit and `metrics.sh` counts it distinctly.

**Depends on**: Phase 3.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T014 [P] [US3] `scripts/tests/test_timeout_outcome.sh` — a stall-terminated harness run sets `PROVENANCE_OUTCOME=timeout` in the sidecar, and `scripts/metrics.sh` reports it under a distinct label (not folded into `error`) (FR-007, SC-005)

### Implementation

- [x] T015 [US3] Add `timeout` to the valid `PROVENANCE_OUTCOME` set in `scripts/lib/run-metrics.sh` (`run_metrics_valid`) (FR-007; data-model outcome table)
- [x] T016 [US3] In the `scripts/ai-paced-run.sh` exit handler, set `PROVENANCE_OUTCOME=timeout` when the stop was stall-caused (hard-cap hit or idle retries exhausted), distinct from the `error` path (FR-007; contracts §4)
- [x] T017 [US3] Aggregate/label `timeout` distinctly in `scripts/metrics.sh` (research D5); make T014 GREEN

**Checkpoint**: run-health telemetry distinguishes "brain stopped responding" from "brain errored".

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T018 [P] Portability: run all `scripts/tests/test_brain_*.sh` on macOS (bash 3.2) and Linux; confirm the pure-bash watchdog + `timeout(1)`/bash-fallback hard-cap give identical PASS (SC-004; research D2)
- [x] T019 [P] Run [quickstart.md](quickstart.md) end-to-end; confirm the integration run stops on its own (outer `timeout` untriggered) and a healthy run shows no added latency (SC-002, SC-003)
- [x] T020 Update the `modes.ai-paced` comments in `run-modes.yml` and record provenance trailers on commits (spec 007, ADR-0010, Principle IV)
- [x] T021 **ESCALATE (human gate)**: decide the `timeout` outcome's ADR home — amend [ADR-0014](../../decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md) or write a new ADR. Do NOT self-approve (Principle IV / governance). Blocks merge, not the code. → **Human decided: new [ADR-0015](../../decisions/ADR-0015-harness-liveness-timeout-outcome.md)** (Status: Proposed; formal acceptance at merge).

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (P1)** → no deps.
- **Foundational (P2)** → after Setup; BLOCKS all stories (defaults every resolution reads).
- **US1 (P3)** → after Foundational; the wrapper + resolution are the MVP and the base for US2/US3.
- **US2 (P4)** and **US3 (P5)** → after US1; independent of each other (different files → parallel).
- **Polish (P6)** → after all desired stories.

### Within a story

- Tests (T003–T005, T010, T014) written and FAILING before their implementation.
- US1: watchdog lib (T006) → wire harness (T007) → resolution (T008) → green (T009).

### Parallel opportunities

- Setup: T001 [P].
- US1 tests: T003, T004 [P] (T005 drives the full harness → sequential).
- US2: T011, T012 [P] (different adapter files); T010 [P] test.
- After US1 is green, US2 and US3 can proceed in parallel (disjoint files).

---

## Implementation Strategy

### MVP (the whole point)

1. Phase 1 Setup → 2 Foundational → 3 US1 (tests green) = **the harness can no longer hang**. This
   alone resolves the 005 incident and is independently shippable.

### Incremental delivery

- + US2 (per-adapter calibration, no false kills) → + US3 (timeout telemetry) → Polish.
- Each phase is a small, reviewable diff (Principle II). Commit per task/group with provenance
  (ADR-0010). Merge and the ADR-0014 decision (T021) stay human gates.

---

## Notes

- No new dependency; a pure-bash fallback covers macOS lacking `timeout(1)` (research D2).
- Reuses the spec-006 run-metrics sidecar + consume-once hook unchanged — only one enum value added.
- The verification gate and the four human gates are untouched (FR-009).
