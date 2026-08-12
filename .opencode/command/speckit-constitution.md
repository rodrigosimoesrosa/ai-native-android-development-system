---
description: Create or update the project constitution from interactive or provided principle inputs, ensuring all dependent templates stay in sync.
agent: build
---
Run the Spec Kit **constitution** workflow for this repo.

The referenced skill below is your authoritative command spec. It was authored for Claude Code —
map any Claude-specific mechanics to your own equivalents:
- "Skill tool" / `/speckit-*` slash invocations → perform the step yourself.
- file reads / writes / edits → your own file tools (never shell out to write files).
- `EXECUTE_COMMAND:` hook blocks → actually run that command yourself and wait for its result.
- `.specify/scripts/**` are tool-neutral — run them as-is.

Non-negotiable guardrails: read @CLAUDE.md and honor the constitution, the ADRs, and the four
human gates (merge / architecture-change / dependency-add / release). Amending the constitution is
itself governance work — stop and escalate; never self-approve a change.

User arguments: $ARGUMENTS

Command specification:
@.claude/skills/speckit-constitution/SKILL.md
