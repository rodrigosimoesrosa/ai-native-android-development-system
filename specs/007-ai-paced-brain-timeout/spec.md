# Feature Specification: ai-paced brain-call liveness timeout

**Feature Branch**: `007-ai-paced-brain-timeout`

**Created**: 2026-08-12

**Status**: Draft

**Input**: Observed failure — during the ai-paced run of spec `005` (brain: opencode → a local
LM Studio model), a single brain invocation hung on one task and produced no output for ~9 hours.
The harness could not recover on its own and required a manual kill. Root cause: the harness wraps
the brain call with **no timeout**, so a *hung* brain (one that never returns) blocks the loop
indefinitely — the `max_fix_iterations` bound only protects against a brain that *returns* a
failure, not against silence.

## Overview

This feature makes the tool-neutral ai-paced harness (`scripts/ai-paced-run.sh`) **incapable of
hanging on a stuck brain**. It adds a liveness discipline to every brain invocation: a stuck brain
is detected, terminated, counted as a failed attempt, and folded into the existing
bounded-retry-then-report behavior — so the run always makes progress or stops and reports, never
freezes.

**Design principle — measure *progress*, not *duration*.** A fixed wall-clock timeout is the wrong
instrument: calibrated for a fast cloud model it would falsely kill a slow-but-healthy local model;
calibrated for a slow local model it would let a fast model hang far too long. What distinguishes
"slow but healthy" from "stuck" is **liveness** — a healthy brain (fast or slow) keeps emitting
output (tokens, tool-calls, log lines); a stuck one goes silent. The primary signal is therefore
**time since the last observed progress**, not total elapsed time.

**Scope boundary (explicit):** this feature covers the harness liveness mechanism, the per-adapter
configuration of its thresholds, the neutral defaults in `run-modes.yml`, and the run-health
outcome vocabulary. It does **not** change the verification gate, the four human gates, the loop's
task semantics, or any adapter's brain logic beyond supplying threshold values.

## Clarifications

### Session 2026-08-12

- Q: Fixed timeout, or progress-based? → A: Progress-based (idle/liveness) as the primary control,
  with an absolute wall-clock hard-cap as a secondary backstop.
- Q: Where do the threshold values live, given cloud vs local latency differ by orders of
  magnitude? → A: The neutral core owns the *mechanism* and a conservative default; each
  `adapters/<tool>/` supplies the value for its own latency profile via the environment (same
  pattern as the existing brain command hand-off). The core stays brain-agnostic.
- Q: How is a timeout recorded? → A: As a failed attempt for retry accounting, and as a distinct
  run-health outcome `timeout` (a sibling of `error`), reusing the spec-006 provenance channel.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - The harness never hangs on a stuck brain (Priority: P1)

An operator starts an ai-paced run and steps away. If the brain gets stuck on a task and stops
producing output, the harness must detect the stall, end that attempt, and continue its
bounded-retry logic — ultimately stopping and reporting rather than freezing indefinitely.

**Why this priority**: This is the whole point. A run that can silently freeze for hours defeats
the "autonomous executor that always makes progress or reports" contract of ai-paced (ADR-0009).

**Independent Test**: Configure the harness with a deliberately stalling brain (a command that
sleeps far past the idle threshold with no output). The harness terminates the brain at the
threshold, counts a failed attempt, and — with no progress — reaches its retry bound and exits with
a report, all within a bounded multiple of the threshold. The run never blocks past that bound.

**Acceptance Scenarios**:

1. **Given** a brain that emits no output for longer than the idle threshold, **When** the harness
   invokes it, **Then** the harness terminates that invocation, logs the stall, and treats it as a
   failed fix attempt (incrementing the per-task attempt counter).
2. **Given** repeated stalls on the same task with no progress, **When** the attempt count exceeds
   `max_fix_iterations`, **Then** the harness stops and reports to a human (the existing on_failure
   path) instead of continuing to block.
3. **Given** a brain that finishes normally before the idle threshold, **When** the harness invokes
   it, **Then** behavior is unchanged from today (no premature termination, no added latency).

---

### User Story 2 - Slow-but-healthy brains are not falsely killed (Priority: P1)

A run driven by a **local** model (slow, but steadily producing output) must run to completion
without the liveness mechanism cutting it off, while a run driven by a **fast cloud** model is held
to a tighter liveness expectation — each adapter carrying the thresholds that fit its latency.

**Why this priority**: Without per-adapter calibration the mechanism is either useless (threshold
too loose) or actively harmful (threshold too tight kills healthy local runs) — the exact tension
that motivated a progress-based design.

**Independent Test**: With the opencode (local) adapter's thresholds, a brain that emits a line
every few seconds over a long total duration runs uninterrupted; with the claude-code (cloud)
adapter's thresholds, the same long-silence brain is cut off quickly. The neutral core uses neither
value directly — it reads whatever the adapter supplies, falling back to the `run-modes.yml`
default when unset.

**Acceptance Scenarios**:

1. **Given** the opencode adapter, **When** it hands off to the harness, **Then** the harness uses
   the adapter-supplied (longer) idle threshold and hard-cap.
2. **Given** the claude-code adapter, **When** it hands off to the harness, **Then** the harness
   uses the adapter-supplied (shorter) idle threshold and hard-cap.
3. **Given** no adapter-supplied value, **When** the harness runs, **Then** it uses the conservative
   neutral default from `run-modes.yml` and the run still cannot hang.

---

### User Story 3 - A stalled run is legible in the provenance ledger (Priority: P2)

When a run is terminated for a stall, its recorded run-health must say so distinctly, so the ledger
(spec 006) distinguishes "the brain returned an error" from "the brain stopped responding."

**Why this priority**: Useful for diagnosis and honest metrics, but the safety guarantee (US1/US2)
delivers value without it.

**Independent Test**: After a stall-terminated run, the run-metrics carry
`Provenance-Outcome: timeout` (not `ok`/`error`), and the metrics aggregation counts it under a
distinct label.

**Acceptance Scenarios**:

1. **Given** a run that ends because the brain stalled past the hard-cap or exhausted retries on
   stalls, **When** its run-health is recorded, **Then** `Provenance-Outcome` is `timeout`.
2. **Given** a run that ends by a brain returning failures (not stalls), **When** its run-health is
   recorded, **Then** `Provenance-Outcome` remains `error` (unchanged).

## Requirements *(mandatory)*

- **FR-001**: Every brain invocation MUST be subject to an **idle-timeout**: if the brain produces
  no new output for longer than a configured number of seconds, the harness MUST terminate that
  invocation.
- **FR-002**: Every brain invocation MUST also be subject to an absolute **hard-cap** on total
  wall-clock duration, as a backstop for a brain that emits output continuously but never completes.
- **FR-003**: A terminated (idle-timeout or hard-cap) invocation MUST be treated as a **failed fix
  attempt**, incrementing the same per-task attempt counter that `max_fix_iterations` bounds, so an
  unrecoverable stall reaches the existing on_failure report path.
- **FR-004**: The idle-timeout and hard-cap values MUST be configurable **per adapter** via the
  environment (the same hand-off channel adapters already use to supply the brain command), so the
  neutral core never hard-codes a brain-specific latency assumption.
- **FR-005**: `run-modes.yml` MUST define conservative **neutral defaults** for both thresholds,
  used when an adapter supplies none; with the defaults in effect the harness still MUST NOT be able
  to hang.
- **FR-006**: A brain that completes before either threshold MUST see **no behavior change and no
  added latency** versus today.
- **FR-007**: When a run ends due to stalls, the recorded run-health `Provenance-Outcome` MUST be a
  distinct value `timeout`, separate from `ok`, `error`, and `cancelled`.
- **FR-008**: On terminating a stalled brain, the harness MUST leave the working tree and the task
  file in a consistent state and MUST NOT commit (unchanged from today's no-commit contract).
- **FR-009**: The four mandatory human gates and the verification-gate semantics MUST be unchanged;
  this feature only bounds the brain invocation.
- **FR-010**: The termination MUST clean up the brain process (no orphaned child processes left
  running after the harness moves on or exits).

### Governing decisions (ADRs)

Implement **within** these; a task that would conflict is a human `architecture-change` gate:

- **ADR-0001** (neutral core, tools as adapters) and **ADR-0008** (`adapters/` layout): thresholds
  are adapter-supplied; the core mechanism is brain-agnostic.
- **ADR-0009** (run modes): this hardens ai-paced's on_failure/bounded-retry contract; it does not
  change the mode's gate policy.
- **ADR-0014** (LLM diagnostic telemetry / outcome vocabulary): adds `timeout` to the run-health
  outcome family that already defers `refusal`/`cutoff`. **Flag for human review** whether this is a
  minor extension recorded in ADR-0014 or warrants its own ADR — do not self-approve.

## Success Criteria *(mandatory)*

- **SC-001**: In a test with a permanently-stalled brain, the harness exits with a report in **no
  more than `(max_fix_iterations + 1) × idle_timeout + hard_cap`** wall-clock time — never
  indefinitely. (Contrast: today it never exits.)
- **SC-002**: A healthy brain emitting output at least once per idle-interval runs to completion
  with **zero** premature terminations.
- **SC-003**: A normally-completing brain shows **no measurable added latency** attributable to the
  liveness mechanism.
- **SC-004**: Switching adapters changes the effective thresholds with **no change to
  `scripts/ai-paced-run.sh`** (only the adapter and/or `run-modes.yml` are touched).
- **SC-005**: A stall-terminated run is recorded with `Provenance-Outcome: timeout` and is counted
  distinctly by the metrics aggregation.
- **SC-006**: After a stall termination, **no orphaned brain child processes** remain.

## Edge Cases

- A brain that emits one line then goes silent → idle-timeout fires from the last line, not from
  start.
- A brain that streams steadily but never finishes → hard-cap fires even though idle never does.
- A brain killed at the idle-timeout mid-file-edit → the partial edit is left in the tree; the next
  attempt (or the human, after on_failure) resolves it (consistent with today's failure handling).
- The default (no adapter value) path → must still bound the run.
- Very short legitimate tasks → thresholds must be far above normal per-step latency to avoid
  false positives.

## Scope & Assumptions

**In scope**: `scripts/ai-paced-run.sh` liveness wrapper; per-adapter threshold env in
`adapters/claude-code/` and `adapters/opencode/`; neutral defaults in `run-modes.yml`; the
`timeout` outcome in `scripts/lib/run-metrics.sh` validation and `scripts/metrics.sh` aggregation;
tests for the above.

**Out of scope**: adaptive/auto-calibrated thresholds (possible later iteration — derive the idle
threshold from observed inter-event latency); any change to the verification gate, the human gates,
the loop's task selection, or a brain's internal logic.

**Assumptions**: the harness can observe the brain's output stream (stdout) as the liveness signal;
`timeout(1)` or an equivalent watchdog is available in the harness environment; adapters continue to
hand off to the harness via the environment.

**Dependencies**: [ADR-0009](../../decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md),
[ADR-0014](../../decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md),
[`run-modes.yml`](../../run-modes.yml), [`scripts/ai-paced-run.sh`](../../scripts/ai-paced-run.sh),
[`scripts/lib/run-metrics.sh`](../../scripts/lib/run-metrics.sh).
