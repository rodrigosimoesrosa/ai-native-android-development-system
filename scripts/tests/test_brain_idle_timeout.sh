#!/usr/bin/env bash
# T003 (US1): the idle watchdog kills a stalled brain. A `stall` fake brain (prints once, then
# sleeps forever) with idle=2s must be killed at ~2s, the wrapper must return 124 (timeout(1)
# convention), and the child `sleep` it spawned must NOT survive the process-group kill.
# Covers FR-001, FR-010, SC-001, SC-006. See specs/007 contracts/harness-liveness.md §3.
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

idle=2
hardcap=60   # large: only the idle limit may trigger here
start="$(date +%s)"
rc=0
MODE=stall run_brain_with_liveness "$idle" "$hardcap" bash "$brain" >"$log" 2>&1 || rc=$?
end="$(date +%s)"
elapsed=$((end - start))

# Return code: killed for inactivity -> 124.
[ "$rc" -eq 124 ] || fail "expected return 124 (idle-kill), got $rc"

# Timing: killed at ~idle, and provably did not hang.
[ "$elapsed" -ge 1 ]  || fail "killed too early ($elapsed s < 1s); not a real idle wait"
[ "$elapsed" -le 8 ]  || fail "killed too late ($elapsed s > 8s); idle watchdog is slow/hung"

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

echo "    ok: stall brain idle-killed at ~${elapsed}s, rc=124, no orphaned child sleep"
