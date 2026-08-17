#!/usr/bin/env bash
# T014 (US3): a stall-terminated run is legible in the ledger. Two assertions:
#   A. after the REAL scripts/ai-paced-run.sh self-stops on a `stall` brain, the run-metrics sidecar
#      records PROVENANCE_OUTCOME=timeout — NOT error (a stall is "brain stopped responding", a
#      diagnostically distinct outcome from "brain errored"). (FR-007, SC-005)
#   B. scripts/metrics.sh reports `timeout` under its own outcome label, not folded into `error`.
# Deterministic: a throwaway repo, the gate stubbed green, a fake stall brain (no real LLM).
# See specs/007 contracts/harness-liveness.md §4–5, research D5.
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

harness="$REPO_ROOT/scripts/ai-paced-run.sh"
brain="$REPO_ROOT/scripts/tests/fixtures/fake-brain.sh"
[ -f "$harness" ] || fail "harness missing: $harness"
[ -f "$brain" ]   || fail "fake brain missing: $brain"

# --- Throwaway repo: the real harness + neutral libs, verification gate STUBBED green so only the
# --- brain step can stall (fast + deterministic; no real gradle/knowledge checks). ---
D="$(mktemp -d)"
trap 'pkill -f "sleep 100000" 2>/dev/null; rm -rf "$D"' EXIT
git -C "$D" init -q
git -C "$D" config user.email t@example.com
git -C "$D" config user.name tester

mkdir -p "$D/scripts/lib" "$D/specs/007-out"
cp "$harness" "$D/scripts/ai-paced-run.sh"
cp -R "$REPO_ROOT/scripts/lib/." "$D/scripts/lib/"
cp "$REPO_ROOT/scripts/metrics.sh" "$D/scripts/metrics.sh"
cp "$REPO_ROOT/run-modes.yml" "$D/run-modes.yml"

for s in check-knowledge check-adapter-boundary; do
  printf '#!/usr/bin/env bash\nexit 0\n' > "$D/scripts/$s.sh"
done
printf '#!/usr/bin/env bash\nexit 0\n' > "$D/gradlew"
chmod +x "$D/gradlew"

# One-line approved plan: a single unchecked task the stall brain will never complete.
printf -- '- [ ] T001 do the thing\n' > "$D/specs/007-out/tasks.md"

export TASKS_FILE="specs/007-out/tasks.md"
export AI_PACED_AGENT_CMD="MODE=stall bash '$brain'"
export AI_PACED_BRAIN_IDLE_TIMEOUT=2
export AI_PACED_BRAIN_HARDCAP=6

# Outer safety bound so a regression can never hang this test.
TO=""
command -v timeout  >/dev/null 2>&1 && TO=timeout
[ -z "$TO" ] && command -v gtimeout >/dev/null 2>&1 && TO=gtimeout

rc=0
if [ -n "$TO" ]; then
  ( cd "$D" && "$TO" 60 bash scripts/ai-paced-run.sh >/dev/null 2>&1 ) || rc=$?
else
  ( cd "$D" && bash scripts/ai-paced-run.sh >/dev/null 2>&1 ) || rc=$?
fi
[ "$rc" -eq 124 ] && fail "outer bound fired — harness hung on the stall brain (not a self-stop)"

# --- A. sidecar outcome: the run stopped because of stalls -> outcome MUST be `timeout`, not `error`.
sidecar="$D/.git/mirabilis/run-metrics.env"
[ -f "$sidecar" ] || fail "no run-metrics sidecar written at $sidecar"
outcome="$(grep '^PROVENANCE_OUTCOME=' "$sidecar" | tail -1 | cut -d= -f2)"
[ "$outcome" = "timeout" ] \
  || fail "stall-terminated run recorded PROVENANCE_OUTCOME='$outcome', expected 'timeout' (FR-007)"

# --- B. metrics.sh labels `timeout` distinctly, not folded into `error`. Seed two commits carrying
# --- Provenance-Outcome trailers (one timeout, one error) and read them back through metrics.sh.
git -C "$D" commit -q --allow-empty -m "seed timeout" -m "Provenance-Outcome: timeout"
git -C "$D" commit -q --allow-empty -m "seed error"   -m "Provenance-Outcome: error"
metrics_out="$( cd "$D" && bash scripts/metrics.sh 2>/dev/null )"

printf '%s\n' "$metrics_out" | grep -qiw timeout \
  || fail "scripts/metrics.sh does not report a distinct 'timeout' outcome (FR-007, SC-005)"

# Distinct, not folded: exactly one `timeout` and one `error` in the outcome distribution.
n_timeout="$(printf '%s\n' "$metrics_out" | grep -Ec '^[[:space:]]*1[[:space:]]+timeout$' || true)"
n_error="$(printf '%s\n' "$metrics_out" | grep -Ec '^[[:space:]]*1[[:space:]]+error$' || true)"
[ "$n_timeout" -eq 1 ] || fail "expected one distinct 'timeout' outcome row in metrics.sh, found $n_timeout"
[ "$n_error"   -eq 1 ] || fail "'timeout' appears folded into 'error' (or error miscounted) in metrics.sh"

echo "    ok: stall-terminated run -> sidecar PROVENANCE_OUTCOME=timeout; metrics.sh labels it distinctly"
