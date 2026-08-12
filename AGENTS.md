# AGENTS.md

This is the entry point for **opencode** (and any AGENTS.md-aware agent). The full, authoritative
guidance for every AI agent in this repo lives in [`CLAUDE.md`](CLAUDE.md) — it is tool-neutral by
design. **Read `CLAUDE.md` now and treat it as binding**; this file only points to it so opencode
picks up the same rules Claude Code does.

## Non-negotiables (see CLAUDE.md for the full text)

- **No implementation without an approved spec.** Implement only an approved `specs/<feature>/tasks.md`.
- **Architecture is decided in [`decisions/`](decisions/) (ADRs).** Implement *within* them; if a task
  conflicts with an ADR, **stop and escalate** — do not invent an architectural decision.
- **Four human gates, never crossed autonomously:** `merge`, `architecture-change`, `dependency-add`,
  `release`. Stop and escalate; never self-approve.
- **Tests are the fitness function** (Constitution Principle III): test-first, green is the default.
- **Package base:** `com.mirabilis`. Quality gate: **detekt** (`config/detekt/`).
- **Provenance trailers** on commits per [ADR-0010](decisions/ADR-0010-automated-provenance-and-metrics.md).

## Spec Kit commands (opencode)

The `/speckit-*` commands mirror the Claude Code skills and are defined in
[`.opencode/command/`](.opencode/command/). Each references its authoritative spec in
`.claude/skills/speckit-*/SKILL.md` (single source of truth — no duplication).
