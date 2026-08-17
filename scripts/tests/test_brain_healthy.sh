#!/usr/bin/env bash
# T010 (US2): slow-but-healthy brains are never falsely killed.
#   (a) a `finish` brain returns 0 through the wrapper with no kill and no measurable added
#       latency vs running it directly (SC-003);
#   (b) a slow brain that prints every idle-1s over several intervals runs to completion and is
#       never idle-killed (SC-002).
# See specs/007 contracts/harness-liveness.md §3.
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

# ---------------------------------------------------------------------------
# (a) healthy fast brain: returns 0, not killed, no measurable added latency.
# ---------------------------------------------------------------------------
idle=3
hardcap=60   # large: neither limit may fire for a fast, healthy brain

# Baseline: the brain run directly (no wrapper).
d_start="$(date +%s)"
MODE=finish bash "$brain" >/dev/null 2>&1
d_end="$(date +%s)"
direct=$((d_end - d_start))

# Same brain through the wrapper.
w_start="$(date +%s)"
rc=0
MODE=finish run_brain_with_liveness "$idle" "$hardcap" bash "$brain" >"$log" 2>&1 || rc=$?
w_end="$(date +%s)"
wrapped=$((w_end - w_start))

[ "$rc" -eq 0 ] || fail "healthy finish brain returned $rc through wrapper (expected 0, no kill)"
grep -q "brain: done" "$log" || fail "wrapper dropped the brain's output (no 'brain: done')"

# No measurable added latency: the wrapper must not add more than a poll-tick's slack (SC-003).
[ "$wrapped" -le $((direct + 2)) ] \
  || fail "wrapper added latency: direct=${direct}s wrapped=${wrapped}s"

echo "    ok: finish brain rc=0, output intact, no added latency (direct=${direct}s wrapped=${wrapped}s)"

# ---------------------------------------------------------------------------
# (b) slow-but-streaming brain: prints every idle-1s over several intervals,
#     never idle-killed, exits 0 (SC-002).
# ---------------------------------------------------------------------------
idle=3
period=$((idle - 1))   # print just under the idle window
intervals=4            # several intervals -> a run several times the idle window
hardcap=60             # large: the hard-cap must never fire for this healthy run

start="$(date +%s)"
rc=0
run_brain_with_liveness "$idle" "$hardcap" \
  bash -c 'for i in $(seq 1 "$1"); do echo "slow: tick $i"; sleep "$2"; done' \
  _ "$intervals" "$period" >"$log" 2>&1 || rc=$?
end="$(date +%s)"
elapsed=$((end - start))

[ "$rc" -eq 0 ] || fail "slow healthy brain returned $rc (expected 0, never killed)"

# It really did stream across several idle windows (not killed early).
[ "$elapsed" -ge $((period * intervals)) ] \
  || fail "slow brain finished too fast (${elapsed}s); intervals were not exercised"

# Every tick made it through -> no premature idle-kill dropped output.
got="$(grep -c '^slow: tick ' "$log" || true)"
[ "$got" -eq "$intervals" ] \
  || fail "expected $intervals ticks, got $got (a tick was cut off -> false idle-kill)"

echo "    ok: slow brain streamed $intervals intervals over ${elapsed}s, rc=0, never killed"
