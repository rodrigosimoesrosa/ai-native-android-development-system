#!/usr/bin/env bash
# ai-paced (ADR-0009) — the autonomous EXECUTOR of an approved plan. TOOL-NEUTRAL harness.
#
# Given an approved tasks.md it works the list to completion: next unchecked task → implement (via the
# pluggable brain) → run the shared verification gate → on green mark it [x] → repeat; correct failures
# within a bound. When the list is empty AND the gate is green it opens a change request. It ESCALATES
# the mandatory human gates and NEVER invents scope — it executes what was approved (methods/run-modes.md).
#
# The brain is $AI_PACED_AGENT_CMD; it receives, in the environment: TASKS (the tasks.md path),
# NEXT_TASK (the next unchecked line), GATE_LOG (current failing-checks output), SPEC. It must edit the
# tree, mark the task [x] in TASKS when done, and NOT commit. Each adapters/<tool>/ provides a brain.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

# Neutral run-metrics helper (spec 006): the harness writes run-health to the session sidecar on exit.
. scripts/lib/run-metrics.sh 2>/dev/null || true
# Liveness wrapper (spec 007): a stuck brain call always *returns* (124) so bounded-retry can advance.
. scripts/lib/brain-watchdog.sh 2>/dev/null || true

POLICY="run-modes.yml"
max_iter="$(awk '/ai-paced:/{f=1} f&&/max_fix_iterations:/{print $2; exit}' "$POLICY" 2>/dev/null)"
max_iter="${max_iter:-5}"
branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo -)"

# Brain-call liveness thresholds (spec 007). Resolution order, highest wins: adapter env →
# run-modes.yml default → hard floor (300/1800). A non-integer or ≤0 value is IGNORED (fall through to
# the next source, never abort) so the loop can never hang on garbled config (FR-004, FR-005).
_is_pos_int() { case "$1" in ''|*[!0-9]*) return 1 ;; esac; [ "$1" -gt 0 ] 2>/dev/null; }
_yml_num() { awk -v k="$1:" '/ai-paced:/{f=1} f&&$1==k{print $2; exit}' "$POLICY" 2>/dev/null; }
_resolve_threshold() { # <env-value> <yml-key> <floor>
  local _v
  for _v in "$1" "$(_yml_num "$2")" "$3"; do
    if _is_pos_int "$_v"; then printf '%s' "$_v"; return 0; fi
  done
  printf '%s' "$3"
}
idle="$(_resolve_threshold "${AI_PACED_BRAIN_IDLE_TIMEOUT:-}" brain_idle_timeout_s 300)"
hardcap="$(_resolve_threshold "${AI_PACED_BRAIN_HARDCAP:-}" brain_hardcap_s 1800)"
# hardcap must be ≥ idle (backstop never fires before the primary control); raise it if a resolved
# pair violates that (data-model validation).
if [ "$hardcap" -lt "$idle" ]; then
  echo "⚠ brain hardcap (${hardcap}s) < idle (${idle}s) — raising hardcap to idle. (spec 007, FR-005)"
  hardcap="$idle"
fi

# --- ai-paced requires an APPROVED plan (the handoff boundary): a tasks.md to execute. ---
TASKS="${TASKS_FILE:-specs/$branch/tasks.md}"
if [ ! -f "$TASKS" ]; then
  echo "⛔ ai-paced needs an approved plan: no tasks.md at '$TASKS'."
  echo "   Run the human-paced loop first (specify → plan → tasks), approve it, then re-run ai-paced."
  exit 4
fi
echo "▶ run mode: ai-paced — executing approved plan: $TASKS  (spec: $branch, max_iter: $max_iter)"

# --- Escalation: the four mandatory gates stay human, even in ai-paced. ---
if git status --porcelain | grep -qE '(libs\.versions\.toml|build\.gradle)'; then
  echo "⛔ human gate tripped (dependency-add / architecture-change) → ESCALATE to a human. (run-modes.yml)"
  exit 3
fi

GATE_LOG="$(mktemp)"
run_start_ts="$(date +%s)"
gate_fail_count=0
_ai_paced_on_exit() {
  _code=$?
  rm -f "$GATE_LOG"
  # Run-health → run-metrics sidecar (spec 006; ADR-0014 §3). Neutral, content-free, never blocks.
  if command -v run_metrics_set >/dev/null 2>&1; then
    _end="$(date +%s)"
    run_metrics_set PROVENANCE_LATENCY_MS "$(( (_end - run_start_ts) * 1000 ))" 2>/dev/null || true
    run_metrics_set PROVENANCE_RETRIES "${total_iter:-0}" 2>/dev/null || true
    run_metrics_set PROVENANCE_ERRORS "${gate_fail_count:-0}" 2>/dev/null || true
    case "$_code" in
      0)   _out=ok ;;
      3|4) _out=cancelled ;;   # escalated to a human / no tasks — not a failure of the run itself
      *)   # a stall-caused stop (hard-cap hit or idle retries exhausted) is diagnostically distinct
           # from a gate/agent error — record it as `timeout`, not `error` (spec 007, FR-007).
           if [ "${stall_stop:-0}" -eq 1 ]; then _out=timeout; else _out=error; fi ;;
    esac
    run_metrics_set PROVENANCE_OUTCOME "$_out" 2>/dev/null || true
  fi
  exit "$_code"
}
trap _ai_paced_on_exit EXIT

run_gate() { # shared verification gate (methods/verify-change.md): guardrails + fast JVM tests
  : > "$GATE_LOG"
  bash scripts/check-knowledge.sh        >>"$GATE_LOG" 2>&1 || return 1
  bash scripts/check-adapter-boundary.sh >>"$GATE_LOG" 2>&1 || return 1
  export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
  ./gradlew :core:test :domain:test --console=plain >>"$GATE_LOG" 2>&1 || return 1
  return 0
}
remaining() { grep -cE '^[[:space:]]*- \[ \]' "$TASKS" 2>/dev/null; }
next_task() { grep -m1 -E '^[[:space:]]*- \[ \]' "$TASKS" 2>/dev/null || true; }

# max_fix_iterations bounds the FIX attempts on the CURRENT task, not the whole run — otherwise a plan
# with more than max_iter tasks could never complete even when every step succeeds. We reset the counter
# whenever the open-task count changes (a task got checked off → progress), and keep a generous global
# safety cap against oscillation.
prev_rem=-1
fix_iter=0
total_iter=0
stall_stop=0   # 1 ⇔ the most recent brain call was killed for a stall (124) — drives the timeout outcome
max_total=$(( 200 ))
while : ; do
  gate_ok=1; run_gate || gate_ok=0
  [ "$gate_ok" -eq 0 ] && gate_fail_count=$((gate_fail_count + 1))
  rem="$(remaining)"; rem="${rem:-0}"

  if [ "$rem" -eq 0 ] && [ "$gate_ok" -eq 1 ]; then
    echo "✓ approved plan complete (0 open tasks) and verification gate GREEN."
    echo "→ on_green: open a change request for human review (merge is a human gate)."
    exit 0
  fi

  # Progress → reset the per-task fix budget; no progress → spend one attempt on the current task.
  if [ "$rem" -ne "$prev_rem" ]; then fix_iter=0; fi
  prev_rem="$rem"
  fix_iter=$((fix_iter + 1))
  total_iter=$((total_iter + 1))
  next="$(next_task)"
  echo "· task-attempt $fix_iter/$max_iter (total $total_iter) — $rem task(s) open, gate=$([ "$gate_ok" -eq 1 ] && echo GREEN || echo RED). Next: ${next:-<fix gate>}"
  if [ "$fix_iter" -gt "$max_iter" ]; then
    echo "⛔ on_failure: current task exceeded $max_iter fix attempts without progress → stop and report to a human. (run-modes.yml)"
    exit 1
  fi
  if [ "$total_iter" -gt "$max_total" ]; then
    echo "⛔ on_failure: global safety cap ($max_total) reached → stop and report to a human."
    exit 1
  fi

  # <AGENT STEP> — the pluggable brain: implement NEXT_TASK, mark it [x] in TASKS, fix the gate; no commit.
  if [ -n "${AI_PACED_AGENT_CMD:-}" ]; then
    echo "  → invoking agent to execute the next task…"
    brain_rc=0
    TASKS="$TASKS" NEXT_TASK="$next" GATE_LOG="$GATE_LOG" SPEC="$branch" \
      run_brain_with_liveness "$idle" "$hardcap" bash -c "$AI_PACED_AGENT_CMD" || brain_rc=$?
    if [ "$brain_rc" -eq 124 ]; then
      stall_stop=1
      echo "  ⏱ brain call stalled (idle ${idle}s / hardcap ${hardcap}s) — killed; counts as a failed attempt. (spec 007, FR-003)"
    else
      stall_stop=0
      [ "$brain_rc" -ne 0 ] && echo "  (agent returned non-zero; re-evaluating anyway)"
    fi
    continue
  fi

  echo "  <AGENT STEP> no AI_PACED_AGENT_CMD set — plug a brain via an adapter (adapters/<tool>/). (ADR-0009)"
  exit 2
done
