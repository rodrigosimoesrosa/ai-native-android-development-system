# Research: ai-paced brain-call liveness timeout

**Phase 0** — resolve the technical unknowns for [plan.md](plan.md). All spec clarifications were
already answered in [spec.md](spec.md) §Clarifications; this file records the *how*.

## D1 — Liveness mechanism: progress-based idle-timeout, not fixed duration

**Decision**: Detect a stuck brain by **inactivity of its stdout stream**, not by total elapsed
time. Run the brain with stdout tee'd to a log file; a background watchdog samples the log's mtime
every `T` seconds and, when `now - mtime > idle_timeout`, kills the brain's **process group**. An
outer `timeout`/alarm bounds total wall-clock (the hard-cap).

**Rationale**: A healthy brain — fast or slow — emits a continuous stream (tokens, tool-call lines,
log lines); a stuck one goes silent. Measuring *time since last progress* auto-normalizes across
latency profiles: a fast cloud model that hangs is caught quickly (silence ≠ work), while a slow
local model that streams stays alive as long as it produces. This is the exact pattern that detected
the 005 hang in operations (a stale-mtime watcher), now promoted into the harness.

**Alternatives considered**:
- *Fixed `timeout N bash -c "$BRAIN"`* — simplest, but wrong instrument: calibrated for cloud it
  kills a healthy-but-slow local model; calibrated for local it lets a cloud hang linger. Rejected as
  the primary control; **kept as the secondary hard-cap** backstop.
- *Parse token/heartbeat events from the brain* — brain-specific, violates neutral core (V). Rejected.

## D2 — Portability: `timeout(1)` vs pure-bash watchdog

**Decision**: Implement the **idle watchdog in pure bash** (a background subshell + `stat`/`date` +
`kill`), so it works identically on macOS bash 3.2 and Linux. For the **hard-cap**, use `timeout(1)`
when present and fall back to a second bash alarm subshell when it is not (macOS ships no `timeout`).

**Rationale**: CI + developer parity across macOS and Linux (Technical Context). `stat` mtime flags
differ (`stat -f %m` BSD vs `stat -c %Y` GNU) — the watchdog probes once and picks the working form
(the ops watcher already does this).

**Alternatives**: require GNU coreutils on macOS (`brew install coreutils`) — rejected, that is a
dependency-add and an environment burden.

## D3 — Where thresholds live (neutral core vs adapter)

**Decision**: The neutral harness reads two env vars — `AI_PACED_BRAIN_IDLE_TIMEOUT` and
`AI_PACED_BRAIN_HARDCAP` (seconds) — exactly as it already reads `AI_PACED_AGENT_CMD`. Each
`adapters/<tool>/run-ai-paced.sh` exports the values for its latency profile. `run-modes.yml` holds
conservative neutral defaults used when the env is unset.

| | idle-timeout | hard-cap | rationale |
|---|---|---|---|
| `adapters/claude-code` (cloud, fast) | 120 s | 900 s | fast streams; a long silence is a real hang |
| `adapters/opencode` (local, slow) | 420 s | 3600 s | slow tokens + local Explore; be generous |
| `run-modes.yml` default | 300 s | 1800 s | safe middle when no adapter value |

**Rationale**: Honors ADR-0001/0008 — the core owns the mechanism and a default; adapters own the
brain-specific numbers. Swapping brains changes only the adapter + policy, never `ai-paced-run.sh`.

**Alternatives**: adaptive thresholds derived from observed inter-event latency — deferred to a
possible v2 (spec §Scope). Static per-adapter values first: simple, honest, iterable.

## D4 — Accounting: timeout as a failed attempt

**Decision**: A killed (idle or hard-cap) invocation returns non-zero from the wrapper; the harness
treats it like any failed brain attempt — it increments the existing per-task `fix_iter` and lets
`max_fix_iterations` bound it. No new loop control flow; the wrapper just makes a hang *return*.

**Rationale**: The root cause of the 005 incident is that a hang never *returned*, so `fix_iter`
never advanced. Making the wrapper always return re-arms the existing, tested bound (SC-001).

## D5 — Run-health outcome: `timeout` distinct from `error`

**Decision**: Add `timeout` to the valid `PROVENANCE_OUTCOME` set in `scripts/lib/run-metrics.sh`
(`run_metrics_valid`) and to the aggregation labels in `scripts/metrics.sh`. When the run ends
because of stalls (hard-cap hit, or retries exhausted on idle-timeouts), the exit handler sets
`PROVENANCE_OUTCOME=timeout` instead of `error`.

**Rationale**: Honest, queryable telemetry (spec 006 / ADR-0013/0014): "the brain stopped responding"
is diagnostically different from "the brain returned failures." ADR-0014 already deferred
`refusal`/`cutoff` as future outcome values — `timeout` joins that family. **Flagged** for human
review at merge (minor ADR-0014 extension vs its own ADR).

## D6 — Process cleanup (no orphans)

**Decision**: Launch the brain in its **own process group** (`set -m` / `setsid`-equivalent) and, on
timeout, `kill -TERM -<pgid>` then `kill -KILL -<pgid>` after a short grace. Verify no `opencode`/
child survives (SC-006).

**Rationale**: The brain (e.g. `opencode run`) spawns children (model client, sub-agents); killing
only the top PID leaves orphans holding the model/server. The 005 kill needed a `pkill` sweep — the
wrapper must do this cleanly itself. macOS bash 3.2 lacks `setsid`; use `kill -- -PGID` after
starting the brain via a job in its own group.

## Open items for the human merge gate

- The `timeout` outcome's ADR home (ADR-0014 amendment vs new ADR) — do not self-approve (Principle
  IV / governance).
