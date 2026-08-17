# Contract: harness liveness wrapper

**Phase 1.** The interface surfaces this feature exposes. Three contracts: the **core↔adapter env
contract**, the **`run-modes.yml` schema addition**, and the **watchdog wrapper API**.

## 1. Core ↔ adapter environment contract

The neutral harness (`scripts/ai-paced-run.sh`) reads these; each `adapters/<tool>/run-ai-paced.sh`
may export them. This is the *only* channel — the core never names a brain.

| Variable | Set by | Read by | Default if unset |
|---|---|---|---|
| `AI_PACED_AGENT_CMD` | adapter | harness | (existing — the brain command) |
| `AI_PACED_BRAIN_IDLE_TIMEOUT` | adapter | harness | `run-modes.yml` → floor 300 |
| `AI_PACED_BRAIN_HARDCAP` | adapter | harness | `run-modes.yml` → floor 1800 |

**Invariant**: unset/invalid → the harness falls back down the resolution order and still cannot
hang. The harness MUST NOT branch on *which* adapter set the value.

## 2. `run-modes.yml` schema addition

Under `modes.ai-paced` (neutral defaults; adapters may override via env):

```yaml
modes:
  ai-paced:
    # ...existing keys...
    autonomous_stop:
      on_green: open-change-request
      on_failure: bounded-retry-then-report
      max_fix_iterations: 5
    brain_idle_timeout_s: 300   # NEW: kill a brain call silent this long (primary)
    brain_hardcap_s: 1800       # NEW: kill a brain call running this long total (backstop)
```

Parsed with the same `awk` approach already used for `max_fix_iterations`.

## 3. Watchdog wrapper API (`scripts/lib/brain-watchdog.sh`)

Sourced by the harness. One public function.

```sh
# run_brain_with_liveness <idle_s> <hardcap_s> <cmd...>
#   Runs <cmd...> in its own process group, streaming its stdout through unchanged (tee to the
#   harness log so callers see live output).
#   Returns:
#     0    brain exited 0
#     N>0  brain exited non-zero (N)
#     124  brain was killed for inactivity (idle) OR total time (hardcap)   [timeout(1) convention]
#   Guarantees:
#     - kills the whole process group on timeout (no orphans; SC-006)
#     - no premature kill while stdout advances within idle_s (SC-002)
#     - adds no measurable latency when the brain finishes first (SC-003)
#   Portability: pure-bash idle watchdog (macOS bash 3.2 + Linux); timeout(1) used for hardcap when
#   present, bash-alarm fallback otherwise.
```

### Behavioral contract (asserted by tests)

| Scenario | Input (fake brain) | Expected |
|---|---|---|
| healthy fast | prints, exits 0 in < idle | return 0, no kill, no added delay |
| healthy slow | prints a line every (idle−ε), runs > hardcap? no → < hardcap | return 0, never killed |
| idle stall | prints once, then sleeps ≫ idle | killed at ~idle, return 124, no orphans |
| chatty runaway | prints every 1 s forever | killed at ~hardcap, return 124, no orphans |
| non-zero exit | prints, exits 3 | return 3 (passed through, not 124) |

## 4. Harness integration points (`scripts/ai-paced-run.sh`)

- Replace the direct `bash -c "$AI_PACED_AGENT_CMD"` at the brain-step with
  `run_brain_with_liveness "$idle" "$hardcap" bash -c "$AI_PACED_AGENT_CMD"`.
- On return `124`: log the stall, `continue` the loop (counts as a failed `fix_iter` attempt).
- Track whether the run's stop was stall-caused; if so, the exit handler sets
  `run_metrics_set PROVENANCE_OUTCOME timeout` (instead of `error`).

## 5. Metrics vocabulary (`scripts/lib/run-metrics.sh`, `scripts/metrics.sh`)

- `run_metrics_valid PROVENANCE_OUTCOME <v>` accepts `ok|error|cancelled|timeout`.
- `scripts/metrics.sh` reports a distinct count/label for `timeout`.
