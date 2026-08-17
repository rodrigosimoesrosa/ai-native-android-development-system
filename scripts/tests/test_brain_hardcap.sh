#!/usr/bin/env bash
# T004 (US1): the hard-cap kills a runaway brain. A `runaway` fake brain (prints every 1s
# forever, so it is never idle) with hardcap=4s must be killed at ~4s and the wrapper must
# return 124 (timeout(1) convention). The idle limit is set large so ONLY the hard-cap can
# fire. The child `sleep` it spawned must NOT survive the process-group kill.
# Covers FR-002 (and SC-006). See specs/007 contracts/harness-liveness.md §3.
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

lib="$REPO_ROOT/scripts/lib/brain-watchdog.sh"
brain="$REPO_ROOT/scripts/tests/fixtures/fake-brain.sh"
[ -f "$lib" ]   || fail "watchdog lib missing: $lib"
[ -f "$brain" ] || fail "fake brain missing: $brain"

# shellcheck source=/dev/null
. "$lib"
command -v run_brain_with_liveness >/dev/null 2>&1 \
  || fail "run_brain_with_liveness not defined after sourcing $lib"

log="$(mktemp)"
trap 'rm -f "$log"' EXIT

# Baseline: any pre-existing marker sleeps (e.g. leftovers) are not ours.
before="$(pgrep -f 'sleep 100000' 2>/dev/null | sort || true)"

idle=60      # large: the runaway prints every 1s, so idle must never trigger here
hardcap=4    # only the hard-cap may stop this brain
start="$(date +%s)"
rc=0
MODE=runaway run_brain_with_liveness "$idle" "$hardcap" bash "$brain" >"$log" 2>&1 || rc=$?
end="$(date +%s)"
elapsed=$((end - start))

# Return code: killed for exceeding the hard-cap -> 124.
[ "$rc" -eq 124 ] || fail "expected return 124 (hardcap-kill), got $rc"

# Timing: killed at ~hardcap, and provably not by the (large) idle limit.
[ "$elapsed" -ge 3 ]   || fail "killed too early ($elapsed s < 3s); not a real hard-cap wait"
[ "$elapsed" -le 10 ]  || fail "killed too late ($elapsed s > 10s); hard-cap is slow/hung"

# No orphan: the spawned child sleep must die with the process group (SC-006).
# Poll briefly for TERM->KILL to settle, then diff against the baseline.
new=""
for _ in 1 2 3 4 5 6 7 8 9 10; do
  after="$(pgrep -f 'sleep 100000' 2>/dev/null | sort || true)"
  new="$(comm -13 <(printf '%s\n' "$before") <(printf '%s\n' "$after") | tr -d '[:space:]')"
  [ -z "$new" ] && break
  sleep 0.5
done
[ -z "$new" ] || fail "orphan child sleep survived the kill (pids: $new)"

echo "    ok: runaway brain hardcap-killed at ~${elapsed}s, rc=124, no orphaned child sleep"
