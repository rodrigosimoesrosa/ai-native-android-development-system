#!/usr/bin/env bash
# ai-paced with Claude Code as the brain (ADR-0009). Only this file is tool-specific; the harness
# (scripts/ai-paced-run.sh), gate, escalation, and provenance are shared/unchanged.
#
# Prereq: the `claude` CLI available and authenticated, with permission to edit files.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

command -v claude >/dev/null 2>&1 || {
  echo "✗ claude CLI not found. Install Claude Code, then re-run."
  exit 2
}

export PROVENANCE_AGENT="claude-code"
export PROVENANCE_MODEL="${PROVENANCE_MODEL:-claude-sonnet-5}"
PROMPT='You are the autonomous executor of an approved plan. Implement the next task ($NEXT_TASK) from $TASKS for spec $SPEC by editing files in this repo. When it passes the checks, mark that task [x] in $TASKS. If checks fail, read $GATE_LOG and fix. Do not commit. Keep changes minimal and in-scope.'
# Token capture (spec 006 / ADR-0013): each brain invocation accumulates its token usage into the
# run-metrics sidecar. Best-effort — parses Claude Code's JSON `usage`; if absent, tokens are omitted
# (never faked). This is the only tool-specific piece; the neutral core stamps + aggregates it.
BRAIN="claude -p --output-format json \"$PROMPT\""
export AI_PACED_AGENT_CMD='. scripts/lib/run-metrics.sh 2>/dev/null || true; __o="$(mktemp)"; '"$BRAIN"' | tee "$__o"; __in="$(grep -oE "\"input_tokens\"[ :]*[0-9]+" "$__o" | grep -oE "[0-9]+" | head -1)"; __out="$(grep -oE "\"output_tokens\"[ :]*[0-9]+" "$__o" | grep -oE "[0-9]+" | head -1)"; __sum=$(( ${__in:-0} + ${__out:-0} )); [ "${__sum:-0}" -gt 0 ] && run_metrics_add_tokens "$__sum"; rm -f "$__o"'

echo "▶ launching ai-paced — brain: claude-code ($PROVENANCE_MODEL)"
exec bash scripts/ai-paced-run.sh
