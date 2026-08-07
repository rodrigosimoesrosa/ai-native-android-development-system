# ADR-0001: Neutral core in git, AI tools as pluggable adapters

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [[00-vision-and-architecture]], ADR-0002 (spec format — created), ADR-0003 (knowledge-graph representation — to be created)

---

## Context

While defining the next deliverables, the right question surfaced:

> "Don't Claude Code and Spec Kit already do this for me? And if tomorrow I want to use
> opencode instead of Claude Code, will this structure still work?"

Two tensions:

1. **NIH (not-invented-here) risk.** Reinventing a spec format (Spec Kit already provides one)
   or a Skills mechanism (Claude Code already provides one) would spend the novelty budget in
   the wrong place and weaken the project as a Staff/Principal portfolio piece. Buy > build.

2. **Lock-in risk.** If the project's intelligence (specs, knowledge, method) lives *inside*
   a tool's proprietary features, swapping agents (Claude Code → opencode, Cursor, Copilot,
   Gemini) would require rebuilding the project. This contradicts the vendor-neutrality
   non-goal defined in the vision.

The question "what if I swap tools tomorrow?" is the stress test for the whole design. A
project whose answer is "then everything breaks" is not an AI-native engineering reference —
it is coupling in disguise.

## Decision

We adopt two rules, joint and inseparable:

### 1. Compose, don't reinvent
- **Specs:** we adopt **Spec Kit** as the spec-driven development mechanism. We do not create our own format.
- **AI capabilities (Skills):** we adopt **Claude Code Skills** as *one* adapter. We do not build our own agent runtime.
- **Gates/automation:** we use the tool's native mechanisms (permissions, hooks, CI) instead of our own orchestrator.

### 2. Neutral core in git + tool as a thin adapter
Knowledge and method live in **neutral files in git**. The AI tool is a **pluggable adapter
layer** on top. The boundary is explicit and mandatory:

```
┌───────────────────────────────────────────────────────────┐
│  NEUTRAL CORE (git) — owns the project's intelligence      │
│  · specs/          (Spec Kit — tool-agnostic format)       │
│  · decisions/      (ADRs in markdown)                      │
│  · knowledge/      (glossary, module contracts, links)     │
│  · methods/        (the "how" of each skill, neutral prose)│
│  · app/            (modularized Android code)              │
│  · tests + CI      (executable contract, neutral)          │
└───────────────────────────┬───────────────────────────────┘
                            │ invoked by ↓ (thin, disposable)
┌───────────────────────────▼───────────────────────────────┐
│  ADAPTER LAYER (tool-specific)                             │
│  · adapters/claude-code/   (SKILL.md, hooks, settings)     │
│  · adapters/opencode/      (equivalent — created when/if   │
│                             a real need appears)           │
└───────────────────────────────────────────────────────────┘
```

**Golden rule:** a Skill is not a capability that *lives* inside Claude Code. It is a
**method documented in `methods/`** (neutral) that an adapter merely *invokes*. The adapter
holds only packaging/invocation — zero domain logic.

## Consequences

### Positive
- **~85% of the structure is immune to a tool swap** (specs, knowledge, architecture, tests, CI). Swapping Claude Code → opencode = writing a new `adapters/` directory, not rewriting the project.
- **Verifiable portability, not promised.** The boundary is a directory rule a reviewer (human or agent) can check.
- **Faster delivery:** no effort spent rebuilding Spec Kit / a Skills runtime.
- **Stronger portfolio:** demonstrates the mature "buy > build" and "design for portability" instincts — exactly what is assessed at Staff/Principal.

### Negative / costs
- **Ongoing discipline required.** It is easy for an agent to couple proprietary features unnoticed. We need a guardrail (see Actions).
- **Some duplication:** the method lives in `methods/` (neutral) and is referenced by the adapter — one extra level of indirection.
- **We depend on third-party evolution** (Spec Kit, Claude Code). Acceptable: they are replaceable by design.

### Neutral
- The ~15% coupled parts (skill packaging, hooks, settings) are isolated and explicitly disposable under `adapters/`.

## Alternatives considered

1. **Build our own SDD system (spec format + skills runtime).**
   Rejected: NIH, slow, spends novelty in the wrong place, and would still need an agent to run.

2. **Couple everything to Claude Code (freely use proprietary features).**
   Rejected: lock-in; breaks on the first tool swap; contradicts the neutrality thesis.

3. **Support multiple tools from day one (Claude Code + opencode in parallel in v1).**
   Rejected for now: the cost of maintaining two adapters without a proven need. The
   architecture *allows* it; we create the second adapter only when a real need appears (it
   would prove portability — a natural candidate for v3).

## Resulting actions

- [ ] Create `methods/` (neutral) and `adapters/claude-code/` (tool-specific) directories with the boundary documented.
- [ ] Add a **CI guardrail**: verify that no file outside `adapters/` references tool-specific mechanisms (simple grep lint).
- [ ] ADR-0002: spec format — confirm adoption of Spec Kit and how it fits into `specs/`.
- [ ] ADR-0003: knowledge-graph representation (files in git — already decided in the vision, formalize).
- [ ] Update `00-vision-and-architecture.md` to make the adapter layer explicit. ✅
