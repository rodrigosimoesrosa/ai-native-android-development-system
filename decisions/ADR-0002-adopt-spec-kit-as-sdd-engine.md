# ADR-0002: Adopt GitHub Spec Kit as the Spec-Driven Development engine

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [[ADR-0001-build-on-existing-tools-neutral-core]], [[00-vision-and-architecture]], project constitution (`.specify/memory/constitution.md`)

---

## Context

[ADR-0001](ADR-0001-build-on-existing-tools-neutral-core.md) established the "compose, don't
reinvent" and "neutral core + tool as adapter" stance. What remained was to record the
concrete decision: **which** spec tool to adopt and **how** it fits.

[GitHub Spec Kit](https://github.com/github/spec-kit) was evaluated and verified on the
development machine. Facts established (not from memory — checked via the installed CLI):

- Installed version: **`specify 0.12.2`**.
- Supports **30+ agents** via `specify init --integration <agent>`. `specify check` confirmed
  that **Claude Code** and **opencode** are both *available* on this machine.
- Has a **skills mode**: for Claude, `specify init` installs *agent skills* by default (not
  slash-command prompts). This realizes ADR-0001's "neutral method → tool skill" mapping
  **out of the box** — Spec Kit itself generates the per-agent adapter layer.
- In-place init: `specify init . --integration claude` (used `--force` because the repo was not empty).

This decision is *distinct* from ADR-0001: that one set the **strategy** (neutral core +
adapter); this one records the **concrete tool choice** and its adoption.

## Decision

Adopt **GitHub Spec Kit** as the project's SDD engine, in **skills mode** for Claude Code.

Canonical flow (the constitution's "The Loop" principle):
`/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`,
with `/speckit-constitution` for principles and `/speckit-converge` to reconcile the codebase.
Optional quality skills: `/speckit-clarify`, `/speckit-analyze`, `/speckit-checklist`.

Installed structure (executed 2026-08-07):
- `.specify/` — templates, scripts, workflows, memory (constitution), integrations. **Neutral core** → versioned.
- `.claude/skills/speckit-*` — Claude Code **adapter layer** → versioned (reproducible by clone).
- `.claude/settings.local.json` — local settings / possible credentials → **git-ignored** (per Spec Kit's own security notice).

## Relationship to the constitution (why both exist)

- The **constitution** *asserts the rule* (Principle V: neutral core, tool as adapter; and the
  Development Loop). It is forward-looking governance.
- This **ADR** *records and justifies the decision* that produced the rule: which tool, version,
  alternatives, consequences. It is backward-looking provenance.
- The constitution itself requires amendments to come with an ADR and requires "why does this
  exist?" to have a linkable answer (Principle IV). This ADR is that answer for the Spec Kit adoption.

## Consequences

### Positive
- **Zero effort** rebuilding a spec format or a skills runtime.
- **Proven portability, not promised:** `opencode` already shows up as an available integration;
  swapping tools = `specify init . --integration opencode --force`, with the neutral core intact.
- Skills mode already produces ADR-0001's core/adapter boundary automatically.

### Negative / costs
- **Coupling to Spec Kit's evolution** (v0.12.2, pre-1.0 — the API may change). Mitigated: templates
  are versioned in the repo (`--force` regenerates); Spec Kit is replaceable by design.
- `.claude/` may accumulate tool files; requires `.gitignore` discipline (done).
- Spec Kit's slash commands are not themselves neutral — but they are regenerable per agent.

### Neutral
- Spec Kit's default templates may need tuning to reflect the constitution; divergences go to future ADRs.

## Alternatives considered

1. **Our own spec format + our own runtime.** Rejected in ADR-0001 (NIH, slow, would still need an agent).
2. **Another SDD toolkit.** Not evaluated in depth: Spec Kit is agent-neutral, open-source, active,
   and already installed/working here — it meets the requirement at the lowest cost.
3. **Slash-command prompts instead of skills.** Rejected: skills mode is Claude's default and better
   materializes the method/adapter separation.

## Resulting actions

- [x] `specify init . --integration claude --force` executed (2026-08-07).
- [x] Constitution ratified in `.specify/memory/constitution.md` (v1.0.0) via `/speckit-constitution`.
- [x] `.gitignore` protects `.claude/settings.local.json`; tracks `.claude/skills/`.
- [ ] First real spec via `/speckit-specify` to exercise the loop end-to-end (v1 exit criterion).
- [ ] CI guardrail (ADR-0001, open item #7) enforcing the neutral-core / adapter boundary.
- [ ] Re-evaluate when Spec Kit reaches 1.0 (possible amendment to this ADR).
