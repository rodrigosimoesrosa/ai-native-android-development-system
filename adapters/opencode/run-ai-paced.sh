#!/usr/bin/env bash
# ai-paced with a LOCAL model, brain = opencode → Ollama or LM Studio (ADR-0008/0009).
# This is the neutral-core/adapter guarantee applied to the *brain*: only this file is tool-specific;
# the harness (scripts/ai-paced-run.sh), the gate, escalation, and provenance are unchanged.
#
# Usage:
#   PROVIDER=ollama   MODEL=qwen2.5-coder:7b bash adapters/opencode/run-ai-paced.sh
#   PROVIDER=lmstudio MODEL=qwen2.5-coder    bash adapters/opencode/run-ai-paced.sh
#
# Prereqs: `opencode` installed (https://opencode.ai/install); the local server running with a model
# pulled/loaded; a context window of >= 64k tokens (opencode needs it for file-editing tools — the
# Ollama default 4k/16k is too small).
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

PROVIDER="${PROVIDER:-ollama}"
case "$PROVIDER" in
  ollama)   BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434/v1}";   MODEL="${MODEL:-qwen2.5-coder:7b}" ;;
  lmstudio) BASE_URL="${LMSTUDIO_BASE_URL:-http://localhost:1234/v1}";  MODEL="${MODEL:-qwen2.5-coder}" ;;
  *) echo "✗ unknown PROVIDER='$PROVIDER' (use: ollama | lmstudio)"; exit 2 ;;
esac

# 1) opencode present?
command -v opencode >/dev/null 2>&1 || {
  echo "✗ opencode not installed. Install: curl -fsSL https://opencode.ai/install | bash"
  echo "  Then see adapters/opencode/README.md."
  exit 2
}

# 2) local model server reachable?
if command -v curl >/dev/null 2>&1 && ! curl -sf "$BASE_URL/models" >/dev/null 2>&1; then
  echo "✗ local model server not reachable at $BASE_URL"
  case "$PROVIDER" in
    ollama)   echo "  start it: 'ollama serve' + 'ollama pull $MODEL' (and set a 64k+ context window)." ;;
    lmstudio) echo "  start it: LM Studio → Developer → Start Local Server; load '$MODEL' with 64k+ context." ;;
  esac
  exit 2
fi
echo "✓ $PROVIDER reachable at $BASE_URL (model: $MODEL)"

# 3) opencode config (project-root opencode.json is read by opencode; it is gitignored). Written only
#    if absent, so an existing user config is respected.
if [ ! -f opencode.json ]; then
  cat > opencode.json <<JSON
{
  "\$schema": "https://opencode.ai/config.json",
  "provider": {
    "$PROVIDER": {
      "npm": "@ai-sdk/openai-compatible",
      "name": "$PROVIDER (local)",
      "options": { "baseURL": "$BASE_URL" },
      "models": { "$MODEL": { "name": "$MODEL" } }
    }
  }
}
JSON
  echo "  wrote opencode.json (gitignored) for $PROVIDER/$MODEL"
fi

# 4) the pluggable brain + provenance attribution (ADR-0010)
export PROVENANCE_AGENT="opencode"
export PROVENANCE_MODEL="$PROVIDER/$MODEL"
PROMPT='You are the autonomous executor of an approved plan. Implement the next task ($NEXT_TASK) from $TASKS for spec $SPEC by editing files in this repo. When it passes the checks, mark that task [x] in $TASKS. If checks fail, read $GATE_LOG and fix. Do not commit. Keep changes minimal and in-scope.'
# --auto: auto-approve tool permissions. REQUIRED for headless `opencode run` — without it opencode
# auto-rejects every file read/edit and the executor makes zero progress. Safe here: the brain is
# scoped to this repo/branch and every change passes the gate + human merge review (run-modes.yml).
# Token capture (spec 006 / ADR-0013): accumulate token usage into the run-metrics sidecar.
# Best-effort — opencode's token exposure is runtime-dependent; when it does not report usage, tokens
# are simply omitted (never faked). This is the only tool-specific piece; the neutral core does the rest.
BRAIN="opencode run --auto --model '$PROVIDER/$MODEL' \"$PROMPT\""
export AI_PACED_AGENT_CMD='. scripts/lib/run-metrics.sh 2>/dev/null || true; __o="$(mktemp)"; '"$BRAIN"' | tee "$__o"; __in="$(grep -oE "\"input_tokens\"[ :]*[0-9]+" "$__o" | grep -oE "[0-9]+" | head -1)"; __out="$(grep -oE "\"output_tokens\"[ :]*[0-9]+" "$__o" | grep -oE "[0-9]+" | head -1)"; __sum=$(( ${__in:-0} + ${__out:-0} )); [ "${__sum:-0}" -gt 0 ] && run_metrics_add_tokens "$__sum"; rm -f "$__o"'

echo "▶ launching ai-paced — brain: opencode ($PROVIDER/$MODEL)"
exec bash scripts/ai-paced-run.sh
