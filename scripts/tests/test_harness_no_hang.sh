#!/usr/bin/env bash
# T005 (US1): integration — the FULL harness never hangs on a stuck brain. Drive the REAL
# scripts/ai-paced-run.sh in a throwaway repo with a one-line tasks.md and a `stall` fake brain
# (idle=2s, hardcap=6s), inside an outer 60s bound. The harness MUST stop ITSELF via on_failure
# (bounded-retry exhausted -> exit 1); the outer bound must NOT be what killed it (that would be a
# hang). Covers SC-001. See specs/007 contracts/harness-liveness.md §4.
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

harness="$REPO_ROOT/scripts/ai-paced-run.sh"
brain="$REPO_ROOT/scripts/tests/fixtures/fake-brain.sh"
[ -f "$harness" ] || fail "harness missing: $harness"
[ -f "$brain" ]   || fail "fake brain missing: $brain"

# --- Throwaway repo: the real harness + neutral libs, with the verification gate STUBBED green so
# --- only the brain step can stall (fast + deterministic; no real gradle/knowledge checks). ---
D="$(mktemp -d)"
# Reap any stall-brain child sleeps this run may leave behind before removing the workspace.
trap 'pkill -f "sleep 100000" 2>/dev/null; rm -rf "$D"' EXIT
git -C "$D" init -q
git -C "$D" config user.email t@example.com
git -C "$D" config user.name tester

mkdir -p "$D/scripts/lib" "$D/specs/007-int"
cp "$harness" "$D/scripts/ai-paced-run.sh"
cp -R "$REPO_ROOT/scripts/lib/." "$D/scripts/lib/"
cp "$REPO_ROOT/run-modes.yml" "$D/run-modes.yml"

# Gate stubs: instant + green. The harness runs the gate each loop; here we exercise the brain step.
for s in check-knowledge check-adapter-boundary; do
  printf '#!/usr/bin/env bash\nexit 0\n' > "$D/scripts/$s.sh"
done
printf '#!/usr/bin/env bash\nexit 0\n' > "$D/gradlew"
chmod +x "$D/gradlew"

# One-line approved plan: a single unchecked task the stall brain will never complete.
printf -- '- [ ] T001 do the thing\n' > "$D/specs/007-int/tasks.md"

# The pluggable brain = the stall fixture (prints once, then sleeps forever -> idle stall).
export TASKS_FILE="specs/007-int/tasks.md"
export AI_PACED_AGENT_CMD="MODE=stall bash '$brain'"
export AI_PACED_BRAIN_IDLE_TIMEOUT=2
export AI_PACED_BRAIN_HARDCAP=6

# Outer safety bound: prefer timeout(1)/gtimeout(1); else a pure-bash poll (macOS may lack timeout(1)).
TO=""
command -v timeout  >/dev/null 2>&1 && TO=timeout
[ -z "$TO" ] && command -v gtimeout >/dev/null 2>&1 && TO=gtimeout

start="$(date +%s)"; rc=0; outer_fired=0
if [ -n "$TO" ]; then
  ( cd "$D" && "$TO" 60 bash scripts/ai-paced-run.sh >/dev/null 2>&1 ) || rc=$?
  [ "$rc" -eq 124 ] && outer_fired=1   # 124 == the outer timeout(1) killed it -> the harness hung
else
  ( cd "$D" && bash scripts/ai-paced-run.sh >/dev/null 2>&1 ) &
  hpid=$!
  while kill -0 "$hpid" 2>/dev/null; do
    if [ $(( $(date +%s) - start )) -ge 60 ]; then
      kill -TERM "$hpid" 2>/dev/null; sleep 1; kill -KILL "$hpid" 2>/dev/null
      outer_fired=1; break
    fi
    sleep 0.5
  done
  wait "$hpid" 2>/dev/null || rc=$?
fi
end="$(date +%s)"; elapsed=$((end - start))

# SC-001: the outer bound must NOT be what stopped the run.
[ "$outer_fired" -eq 0 ] \
  || fail "the outer 60s bound killed the harness (${elapsed}s) — it hung on the stall brain (SC-001)"

# The harness must stop ITSELF via on_failure (bounded-retry exhausted -> exit 1).
[ "$rc" -eq 1 ] \
  || fail "expected harness self-stop on_failure (exit 1), got rc=$rc after ${elapsed}s"

# ...and do so well inside the outer window, not right at its edge.
[ "$elapsed" -lt 60 ] \
  || fail "harness ran the full outer window (${elapsed}s) — not a bounded self-stop"

echo "    ok: harness self-stopped (on_failure, rc=1) in ${elapsed}s; outer bound never fired"
