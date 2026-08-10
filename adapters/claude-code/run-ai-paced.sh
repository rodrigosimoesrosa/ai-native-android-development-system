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
export AI_PACED_AGENT_CMD="claude -p \"$PROMPT\""

echo "▶ launching ai-paced — brain: claude-code ($PROVENANCE_MODEL)"
exec bash scripts/ai-paced-run.sh
