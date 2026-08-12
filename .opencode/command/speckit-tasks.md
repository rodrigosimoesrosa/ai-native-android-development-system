---
description: Generate an actionable, dependency-ordered tasks.md for the feature based on available design artifacts.
agent: build
---
Run the Spec Kit **tasks** workflow for this repo.

The referenced skill below is your authoritative command spec. It was authored for Claude Code —
map any Claude-specific mechanics to your own equivalents:
- "Skill tool" / `/speckit-*` slash invocations → perform the step yourself.
- file reads / writes / edits → your own file tools (never shell out to write files).
- `EXECUTE_COMMAND:` hook blocks → actually run that command yourself and wait for its result.
- `.specify/scripts/**` are tool-neutral — run them as-is.

Non-negotiable guardrails: read @CLAUDE.md and honor the constitution, the ADRs, and the four
human gates (merge / architecture-change / dependency-add / release). Stop and escalate — never
self-approve a gate.

User arguments: $ARGUMENTS

Command specification:
@.claude/skills/speckit-tasks/SKILL.md
