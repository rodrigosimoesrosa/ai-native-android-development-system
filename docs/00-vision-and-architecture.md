# AI-Native Android Development System — Vision & Architecture (v0)

> **Status:** Draft 0 · Living document · Owner: project maintainer
> **Purpose:** First architecture document. Defines what this project is, why it exists, and the principles that constrain every later decision.
> **Audience:** Human contributors *and* AI agents. Both are first-class readers.

---

## 0. Framing note (read this first)

This is not "an Android app." Android is the **proving ground**. The product is a
**repeatable system for building software with AI agents** — one that happens to be
demonstrated on a domain (Android) with enough real complexity (Gradle, modularization,
Compose, coroutines, async lifecycles) that the system can't hide behind a toy.

Three claimed identities are in tension and must be ranked, not blended:

| Identity | Optimizes for | Risk if it wins uncritically |
|---|---|---|
| Portfolio piece (Staff/Principal) | Novelty, breadth, narrative | Cleverness over substance; reviewer fatigue |
| Production-ready starter template | Restraint, boring tech, low blast radius | Nothing to show; indistinct from existing templates |
| Reference implementation for AI-assisted dev | Reproducibility, legibility, method | Over-abstraction; a framework nobody asked for |

**Decision:** The *reference implementation* is primary. The portfolio value is a
**consequence** of doing the reference implementation well, not a separate goal to chase.
The starter template is the *artifact* the reference implementation produces. When these
conflict, favor legibility and reproducibility over novelty. This ranking is the single
most important design decision in the document.

**Second decision (see [ADR-0001](../decisions/ADR-0001-build-on-existing-tools-neutral-core.md)):**
This project does **not** build its own spec system or agent runtime. It **composes existing
tools** — Spec Kit for spec-driven development, Claude Code Skills as *one* invocation
adapter — and proves the layer they don't provide (architecture-for-agents, a knowledge
graph, and a measured worked example). The project's intelligence lives in **neutral files
in git**; the AI tool is a **thin, replaceable adapter** on top. Swapping Claude Code for
opencode (or any agent) must mean writing a new `adapters/` directory, never rewriting the
project. Building on existing tools *and* staying tool-neutral is a single, non-negotiable stance.

---

## 1. Vision

> A repository where the **engineering process is a first-class, machine-readable
> artifact** — where specifications, decisions, and knowledge are as versioned,
> reviewable, and executable as the code itself, so that humans and AI agents can
> collaborate on a real Android codebase without either losing the plot.

The long-term bet: the differentiator in software engineering is shifting from *who can
write code* to *who can maintain a legible, verifiable system of intent* that both people
and models can act on safely. This repo is a concrete, opinionated demonstration of that bet.

---

## 2. Mission

Build and openly document an Android codebase where:

1. Every unit of work begins as a **specification** an agent can read, plan against, and verify.
2. Architecture makes **blast radius small and boundaries explicit**, so an agent's change is scoped and reviewable.
3. Project knowledge (decisions, conventions, domain facts) is **captured as structured, linkable data** colocated with the code — not lost in chat logs or a wiki.
4. **AI capabilities (Skills)** are defined, testable, and reusable rather than ad-hoc prompts.
5. A human stays in the loop at **well-defined gates**, with full provenance of what an agent did and why.

---

## 3. Target audience

Ranked by priority:

1. **Staff/Principal engineers & eng leaders** evaluating how to make their codebases AI-workable. Primary readers of the *method*.
2. **AI agents** operating on the repo. First-class *users* — the repo's structure is an API for them.
3. **Android engineers** wanting a modern, well-architected starter. Consumers of the *template*.
4. **Hiring managers / reviewers** assessing the author. They read the narrative and the commit history, not just the code.

> **Assumption challenged:** "Optimize for humans *and* agents equally." These groups
> mostly want the same thing — explicit context, small changes, verifiable outcomes — so
> optimizing for agents is largely a forcing function for human clarity. Where they
> diverge (agents want exhaustive machine-readable context; humans want terse summaries),
> we resolve it with **layered docs**: a short human-facing summary that links to
> exhaustive machine-facing detail. We do not maintain two disconnected sources of truth.

---

## 4. Problems being solved

- **Context loss.** Intent and rationale live in ephemeral chat and a maintainer's head. Agents (and new humans) start cold every time. → *Persistent, structured knowledge.*
- **Unbounded blast radius.** Poorly bounded architecture means any change can touch anything; agents amplify this into large, unreviewable diffs. → *Modularization + explicit contracts.*
- **Prompt sprawl.** AI usage is copy-pasted, unversioned, unrepeatable. → *Skills as versioned, tested capabilities.*
- **Unverifiable AI output.** No shared definition of "done" a machine can check. → *Specs + tests as executable acceptance criteria.*
- **No provenance.** Can't tell what an agent changed, on whose authority, or why. → *Traceability from spec → change → decision.*
- **Process is invisible.** Great engineering process is usually undocumented and unshareable. → *Make the process the artifact.*

---

## 5. Non-goals

- **Not** a novel Android UI showcase or feature-rich demo app. Features exist only to exercise the system.
- **Not** a bespoke AI framework, orchestration engine, or agent runtime. We compose existing tools; we do not build a platform to maintain.
- **Not** model- or vendor-locked. No dependency on a single provider's proprietary features.
- **Not** a fully autonomous "no humans needed" pipeline. Human gates are a feature, not a limitation to remove.
- **Not** a benchmark of "AI wrote 100% of this." Honesty about human/agent split beats a purity claim.
- **Not** a knowledge-graph research project. The graph serves the workflow; it is not the point (see §6).

---

## 6. Design principles

1. **Specs are the source of truth.** Code implements a spec; tests verify it. If code and spec disagree, one of them is a bug.
2. **Small, verifiable units.** Every change maps to a scoped spec with machine-checkable acceptance criteria. Prefer many small reviewable diffs over few large ones.
3. **Explicit boundaries.** Modules expose contracts, hide internals. An agent should be able to change a module knowing exactly what it can and cannot break.
4. **Context colocated with code, in the repo, in git.** Knowledge is a versioned file next to what it describes — not an external service, not a chat log. Reproducible by `git clone`.
5. **Layered legibility.** Terse for humans on top; exhaustive machine-readable detail underneath; both generated/checked from one source where possible.
6. **Human-in-the-loop at gates, not everywhere.** Define the few points where human judgment is mandatory (merge, architecture change, dependency add). Automate the rest.
7. **Provenance & traceability.** Every change is traceable to a spec and a decision record. "Why does this exist?" always has a linkable answer.
8. **Boring, mainstream tech.** Favor conventional Android/Kotlin choices. Novelty budget is spent on the *process*, not the stack.
9. **Determinism & reproducibility.** Same spec + same inputs → same verifiable outcome. Pin versions; script everything; no "works on my machine."
10. **Tests as executable specification.** The test suite is the machine-readable contract an agent optimizes against.

> **Assumption challenged — the Knowledge Graph.** "Knowledge Graph" often implies a
> graph database and infrastructure to run. That contradicts principle #4 (reproducible by
> clone) and #8 (boring tech). **Default position:** the knowledge graph is a set of
> **structured, cross-linked markdown/YAML files in git** (decisions, specs, glossary,
> module contracts) with typed links between them — a graph *of files*, queryable by
> ripgrep and by agents, buildable into a richer index if and when a real need appears. We
> adopt a graph *database* only when a demonstrated query need justifies the operational
> cost. Start with the cheapest thing that is still a graph.

---

## 7. Success criteria

**Leading indicators (process works):**
- A new agent, given only the repo, can pick up an open spec and produce a passing, in-scope change with no out-of-band context.
- A reader can trace any line of code → its spec → its decision record in ≤3 hops.
- Adding a feature follows the same documented loop every time (spec → plan → implement → verify → record).

**Lagging indicators (it landed):**
- External engineers fork the *method* (specs/skills structure), not just the app.
- The repo is cited/referenced as an example of AI-native engineering.
- Interview/portfolio outcome: it opens Staff/Principal conversations.

**Health metrics:**
- Median diff size stays small; review time per change stays low.
- CI is green as the default state; specs and tests stay in sync.
- Onboarding time (human or agent) to first merged change is short and trending down.

> Where practical, these are measured, not asserted — e.g., a scripted "cold-start agent"
> check in CI that proves the repo is self-sufficient.

---

## 8. Repository philosophy

- **The repo is an API for two kinds of readers.** Directory layout, naming, and docs are designed for machine navigation as much as human browsing.
- **Everything that matters is in git.** Decisions, specs, skills, conventions. No essential knowledge lives only in a SaaS tool or someone's memory.
- **Convention over configuration, documented over implicit.** Predictable structure lets agents navigate without bespoke instructions.
- **Docs are tested.** Where a doc makes a checkable claim (a command, a contract), CI checks it. Rotten docs are treated as broken builds.
- **Honesty over marketing.** The README says what is real, what is aspirational, and where humans did the work. Credibility is the currency for the target audience.
- **The process is the showcase.** The most valuable file in the repo may be the one describing *how* changes get made, not any single feature.

---

## 9. High-level architecture

Two architectures coexist and must be kept distinct: the **engineering-process architecture**
(the reference implementation) and the **Android application architecture** (the proving ground).

### 9.1 Engineering-process architecture (the actual product)

```
┌──────────────────────────────────────────────────────────────┐
│                     KNOWLEDGE LAYER (git)                      │
│   specs/        decisions/ (ADRs)   glossary/   contracts/    │
│   skills/       conventions/        cross-linked, machine-read │
└───────────────┬──────────────────────────────────┬───────────┘
                │ read intent / write provenance    │
┌───────────────▼───────────────┐   ┌───────────────▼───────────┐
│        AGENT WORKFLOW          │   │      HUMAN GATES          │
│  spec → plan → implement →     │   │  merge · arch change ·    │
│  verify → record decision      │   │  dependency add · release │
│  (Skills = reusable capability)│   │  (approval + provenance)  │
└───────────────┬───────────────┘   └───────────────┬───────────┘
                │                                    │
┌───────────────▼────────────────────────────────────▼──────────┐
│                     VERIFICATION LAYER                         │
│   tests (executable spec) · CI · lint/static analysis ·       │
│   "cold-start agent" self-sufficiency check                   │
└───────────────────────────────┬───────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│              ANDROID APPLICATION (see 9.2)                     │
└───────────────────────────────────────────────────────────────┘
```

**Core components:**
- **Specs** — the unit of work. Machine-readable intent + acceptance criteria. Entry point for any agent. **Adopted from Spec Kit**, not reinvented (ADR-0001).
- **Methods** — the neutral, tool-agnostic "how" of each capability, in prose, in `methods/`. A **Skill** (Claude Code) is a thin *adapter* that invokes a method; the intelligence lives in the method, not the adapter.
- **Knowledge graph (of files)** — ADRs, glossary, module contracts, conventions, cross-linked. The persistent memory.
- **Workflow loop** — the one repeatable path: `spec → plan → implement → verify → record`.
- **Gates** — the few mandatory human-judgment points, with approval + provenance.
- **Verification** — tests, CI, static analysis, and a self-sufficiency check.

### 9.1a Neutral core vs. adapter layer (portability guarantee)

The single rule that makes the whole system survive a tool swap (ADR-0001):

```
┌───────────────────────────────────────────────────────────┐
│  NEUTRAL CORE (git) — owns the project's intelligence      │
│  specs/ · decisions/ · knowledge/ · methods/ · app/ · tests│
│  → ~85% of the project. Immune to which AI tool you use.   │
└───────────────────────────┬───────────────────────────────┘
                            │ invoked by ↓ (thin, disposable)
┌───────────────────────────▼───────────────────────────────┐
│  ADAPTER LAYER (tool-specific) — packaging/invocation only │
│  adapters/claude-code/  (SKILL.md, hooks, settings)        │
│  adapters/opencode/     (added only when/if needed)        │
└───────────────────────────────────────────────────────────┘
```

- **Golden rule:** no domain logic in the adapter. If it holds *knowledge or method*, it belongs in the neutral core.
- **Verifiable, not promised:** a CI guardrail checks that nothing outside `adapters/` references tool-specific mechanisms.
- **Swap cost:** Claude Code → opencode = write a new `adapters/` directory. The specs, knowledge, architecture, and tests are untouched.

### 9.2 Android application architecture (the proving ground)

Deliberately conventional so novelty stays in the process:
- **Modularization** by feature + core layers, with explicit module contracts (small blast radius, principle #3).
- **Modern, mainstream stack** (Kotlin, Compose, coroutines/Flow, a standard DI approach). Choices pinned and justified in ADRs.
- **Clear layer boundaries** (UI / domain / data) so a change's scope is legible to an agent.
- The app's job is to be *representative and non-trivial*, not impressive.

### 9.3 Multi-agent model (explicit, because the brief requires it)

> **Assumption challenged:** "Multiple AI agents will interact with the repository"
> implies concurrency and conflict, which most templates ignore.

- **Serialized by default, parallel by design.** Work is decomposed into independent specs with disjoint module scope so agents rarely collide. Git + small diffs are the concurrency primitive.
- **Provenance per change.** Every agent-authored change records which spec, which skill, and which agent/model produced it.
- **Conflict resolution = human gate.** Overlapping changes surface at review, not silently merged.
- **No shared mutable runtime state.** Agents coordinate through the repo (specs, branches, PRs), not a live orchestrator we'd have to build and operate (respects non-goal in §5).

---

## 10. Roadmap

### v1 — "The loop exists and is real"
- One documented, repeatable workflow: `spec → plan → implement → verify → record`.
- Spec format + a handful of ADRs + glossary + module contracts, cross-linked in git.
- A minimal but genuinely modularized Android app exercising the loop end-to-end.
- 2–4 core Skills (write-spec, implement-spec, write-ADR, review-change).
- CI: tests, lint, and a first "cold-start agent self-sufficiency" check.
- Honest README explaining the method and the human/agent split.
- **Exit criterion:** an agent, given only the repo, completes an open spec into a passing, in-scope PR.

### v2 — "It scales past one contributor/agent"
- Multi-agent provenance and disjoint-scope decomposition proven with a parallel example.
- Skills library matures: tested, versioned, with usage docs.
- Knowledge graph gains typed links + a query/index step (still git-first).
- Tighter gates: dependency-add and architecture-change gates with checklists.
- Metrics dashboard: diff size, review time, onboarding-to-first-merge.
- **Exit criterion:** two agents complete two independent features in parallel; provenance and gates hold.

### v3 — "It's a reusable method, not just this repo"
- Extract the process layer into a documented, portable template others can adopt on a *different* codebase (prove Android was incidental).
- **Add a second adapter (`adapters/opencode/`)** to prove tool-neutrality is real, not asserted (ADR-0001). The neutral core stays untouched.
- Optional richer knowledge backend *only if* a real query need was demonstrated in v2.
- Case study / write-up: measured results, what worked, what didn't, honest limits.
- Community: contribution guide written for humans *and* agents.
- **Exit criterion:** an external project adopts the method and reports back.

---

## Decision log

- **[ADR-0001](../decisions/ADR-0001-build-on-existing-tools-neutral-core.md)** — Neutral core in git, AI tools as pluggable adapters. Compose Spec Kit + Claude Code; do not reinvent. *(Accepted)*
- **[ADR-0002](../decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md)** — Adopt GitHub Spec Kit `0.12.2` as the SDD engine (skills mode, Claude Code integration). opencode confirmed available as a swap target. *(Accepted)*
- **[ADR-0003](../decisions/ADR-0003-android-architecture-clean-mvi.md)** — Android architecture: pragmatic Clean Architecture + MVI, pure domain, model-per-layer, typed `Result`/`AppError`. *(Accepted)*
- **[ADR-0004](../decisions/ADR-0004-dependency-injection-hilt.md)** — Dependency Injection with Hilt (on Dagger `2.60.1`); `:app` as composition root, domain stays pure. *(Accepted)*
- **[ADR-0005](../decisions/ADR-0005-local-persistence-room-datastore.md)** — Local persistence: Room `2.8.4` for relational data (`Product` → `ProductEntity`) + Proto DataStore `1.2.1` for single-object/typed state (the current `User` → `UserProto`, settings, flags). Persistence types confined to `:data`, mapped straight to domain; offline-first. *(Accepted)*
- **Constitution** — `.specify/memory/constitution.md` v1.1.0 (amended 2026-08-07: SOLID explicit + architecture baseline). *(Ratified)*

## Open questions (to resolve as ADRs)

1. ~~Exact spec format~~ → **resolved by ADR-0002** (Spec Kit 0.12.2 adopted; templates in `.specify/templates/`).
2. ~~Where "Skills" live~~ → **resolved by ADR-0001** (neutral `methods/` + thin `adapters/`); ADR-0004 to detail method/adapter conventions.
3. Minimum viable knowledge-graph representation before any DB is justified (ADR-0006).
4. What precisely triggers each human gate.
5. How provenance is recorded without polluting git history or the app code.
6. The concrete "cold-start agent self-sufficiency" check in CI.
7. The CI guardrail that enforces the neutral-core / adapter boundary (grep-based lint per ADR-0001).

> Each open item should become an ADR in `decisions/` before the code it governs is written.
> This document is itself the first entry in the knowledge layer it describes.