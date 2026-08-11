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

POLICY="run-modes.yml"
max_iter="$(awk '/ai-paced:/{f=1} f&&/max_fix_iterations:/{print $2; exit}' "$POLICY" 2>/dev/null)"
max_iter="${max_iter:-5}"
branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo -)"

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

GATE_LOG="$(mktemp)"; trap 'rm -f "$GATE_LOG"' EXIT

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
max_total=$(( 200 ))
while : ; do
  gate_ok=1; run_gate || gate_ok=0
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
    TASKS="$TASKS" NEXT_TASK="$next" GATE_LOG="$GATE_LOG" SPEC="$branch" bash -c "$AI_PACED_AGENT_CMD" \
      || echo "  (agent returned non-zero; re-evaluating anyway)"
    continue
  fi

  echo "  <AGENT STEP> no AI_PACED_AGENT_CMD set — plug a brain via an adapter (adapters/<tool>/). (ADR-0009)"
  exit 2
done
