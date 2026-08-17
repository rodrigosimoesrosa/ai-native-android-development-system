# Quickstart: validate the ai-paced brain-call liveness timeout

**Phase 1.** Runnable checks that prove the feature works end-to-end. All use a **fake brain** (no
real LLM), so they are deterministic and fast. See [contracts/harness-liveness.md](contracts/harness-liveness.md).

## Prerequisites

- Repo checked out; `bash` available (macOS 3.2 or Linux ≥ 4).
- No LM Studio/Ollama needed — the tests inject a fake brain via `AI_PACED_AGENT_CMD`.

## The fake brain

`scripts/tests/fixtures/fake-brain.sh` streams/sleeps on demand:

```sh
MODE=finish  bash scripts/tests/fixtures/fake-brain.sh   # prints a few lines, exits 0
MODE=stall   bash scripts/tests/fixtures/fake-brain.sh   # prints once, then sleeps forever
MODE=runaway bash scripts/tests/fixtures/fake-brain.sh   # prints every 1s forever
MODE=fail    bash scripts/tests/fixtures/fake-brain.sh   # prints, exits 3
```

## Unit-level: the watchdog wrapper

```sh
# idle stall killed at ~idle, returns 124, no orphan children
bash scripts/tests/test_brain_idle_timeout.sh     # expect: PASS (killed ~2s with idle=2)

# runaway killed at ~hardcap, returns 124
bash scripts/tests/test_brain_hardcap.sh          # expect: PASS

# healthy fast brain: no premature kill, return 0, no added latency
bash scripts/tests/test_brain_healthy.sh          # expect: PASS
```

Each test uses tiny thresholds (e.g. idle=2 s, hardcap=4 s) so the suite runs in seconds.

## Integration: the harness never hangs

Drive the real loop with a stalling fake brain and assert it **stops and reports** instead of
blocking:

```sh
TASKS_FILE=$(mktemp);  printf -- '- [ ] T001 do a thing\n' > "$TASKS_FILE"
AI_PACED_BRAIN_IDLE_TIMEOUT=2 AI_PACED_BRAIN_HARDCAP=6 \
AI_PACED_AGENT_CMD='MODE=stall bash scripts/tests/fixtures/fake-brain.sh' \
TASKS_FILE="$TASKS_FILE" \
  timeout 60 bash scripts/ai-paced-run.sh; echo "exit=$?"
# EXPECT: exits (on_failure after max_fix_iterations) within ~ (max_iter+1)*idle + hardcap,
#         NEVER blocks; outer `timeout 60` must NOT be what stops it.
```

## Outcome telemetry

After a stall-terminated run:

```sh
grep -c 'PROVENANCE_OUTCOME=timeout' .git/mirabilis/run-metrics.env 2>/dev/null   # 1 (before consume)
# or on the resulting commit trailer:
git log -1 --format='%b' | grep 'Provenance-Outcome: timeout'                     # present
bash scripts/metrics.sh | grep -i timeout                                         # counted distinctly
```

## Portability check (macOS + Linux)

Run the three unit tests on both platforms; the pure-bash idle watchdog and the `timeout(1)`/bash
hard-cap fallback must give identical PASS results (SC-004).

## No-regression check

A healthy brain shows unchanged behavior and no added latency (SC-002/SC-003):

```sh
AI_PACED_AGENT_CMD='MODE=finish bash scripts/tests/fixtures/fake-brain.sh' \
  bash scripts/tests/test_brain_healthy.sh    # PASS with ~0 overhead vs running the brain directly
```

## Done when

- All `scripts/tests/test_brain_*.sh` pass on macOS and Linux.
- The integration run stops on its own (not via the outer `timeout`).
- A stall-terminated run records `Provenance-Outcome: timeout`, counted distinctly by `metrics.sh`.
