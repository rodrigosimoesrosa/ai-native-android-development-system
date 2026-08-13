#!/usr/bin/env bash
# T001: parametrized fake brain for the ai-paced liveness tests (no real LLM).
# Deterministic stand-in driven by MODE. The stalling/runaway modes spawn a
# long-lived child `sleep` in the same process group so a watchdog process-group
# kill is exercised: if the group is TERM/KILLed the child must die with it; if
# it survives, the tests detect an orphan (SC-006). See specs/007 quickstart +
# contracts/harness-liveness.md §3.
#
#   MODE=finish    prints a few lines, exits 0        (healthy fast brain)
#   MODE=stall     prints once, then sleeps forever   (idle stall -> idle-timeout)
#   MODE=runaway   prints every 1s forever            (never idle -> hardcap)
#   MODE=fail      prints, exits 3                     (brain error, passed through)
set -u

MODE="${MODE:-finish}"

# A distinctive long-lived child in this process group. A group kill must reap
# it; the tests grep for this sleep to assert no orphan survives.
spawn_child() {
  sleep 100000 &
}

case "$MODE" in
  finish)
    echo "brain: starting"
    echo "brain: working"
    echo "brain: done"
    exit 0
    ;;
  stall)
    spawn_child
    echo "brain: starting"
    # stdout stops advancing here: the idle watchdog must notice and kill.
    while :; do sleep 100000; done
    ;;
  runaway)
    spawn_child
    # Never idle, never finishes -> only the hardcap can stop it.
    i=0
    while :; do
      i=$((i + 1))
      echo "brain: tick $i"
      sleep 1
    done
    ;;
  fail)
    echo "brain: starting"
    echo "brain: boom" >&2
    exit 3
    ;;
  *)
    echo "fake-brain: unknown MODE '$MODE'" >&2
    exit 2
    ;;
esac
