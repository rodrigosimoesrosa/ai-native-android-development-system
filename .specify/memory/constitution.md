# AI-Native Android Development System Constitution

<!-- Governs how humans and AI agents build in this repository. Derived from
     docs/00-vision-and-architecture.md and decisions/ADR-0001. This file is the
     operational contract every /speckit workflow must respect. -->

## Core Principles

### I. Specs Are the Source of Truth (NON-NEGOTIABLE)
Every unit of work begins as a specification an agent can read, plan against, and verify.
Code implements a spec; tests verify it. If code and spec disagree, one of them is a bug —
fix the disagreement, never ignore it. No implementation starts without an approved spec.

### II. Small, Verifiable Units with Explicit Boundaries
Prefer many small, reviewable diffs over few large ones. Every change maps to a scoped spec
with machine-checkable acceptance criteria. Modules expose contracts and hide internals so a
change's blast radius is small and legible. An agent must be able to change a module knowing
exactly what it can and cannot break.

### III. Tests as Executable Specification (NON-NEGOTIABLE)
The test suite is the machine-readable contract an agent optimizes against. Acceptance
criteria are expressed as tests wherever possible. A change is "done" only when its tests
pass in CI. Green CI is the default state; a red build is treated as broken work, not progress.

### IV. Knowledge in Git, with Provenance
All essential knowledge — specs, decisions (ADRs), glossary, module contracts, conventions —
lives as versioned, cross-linked files in the repository, reproducible by `git clone`. No
essential context lives only in a chat log, a SaaS tool, or someone's memory. Every change is
traceable to its spec and its decision record: "why does this exist?" always has a linkable answer.

### V. Neutral Core, Tools as Pluggable Adapters
The project's intelligence lives in tool-agnostic files in git (see ADR-0001). AI tooling
(Spec Kit, Claude Code, opencode, …) is a thin, replaceable adapter on top. No domain logic
or project knowledge may live inside a tool-specific adapter. Swapping the AI tool must mean
regenerating an adapter, never rewriting the project. Favor boring, mainstream technology; the
novelty budget is spent on the engineering *process*, not the stack.

## Additional Constraints — Architecture & Reproducibility

- **Android is the proving ground, not the product.** Features exist only to exercise the
  system; the app must be representative and non-trivial, never a feature showcase.
- **Modularization by feature + core layers**, with explicit module contracts and clear
  UI / domain / data boundaries so change scope is legible to an agent.
- **Determinism & reproducibility:** pin versions, script everything, justify stack choices in
  ADRs. Same spec + same inputs → same verifiable outcome.
- **Layered legibility:** terse human-facing summaries on top, exhaustive machine-readable
  detail underneath, from a single source of truth. Never maintain two disconnected truths.
- **Knowledge graph starts as files** (markdown/YAML with typed links). Adopt a graph database
  only when a demonstrated query need justifies the operational cost.

## Development Workflow — The Loop & Gates

- **The one repeatable loop:** `spec → plan → implement → verify → record`, driven by Spec Kit
  (`/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`), closed by
  recording a decision when one was made.
- **Human-in-the-loop at gates, not everywhere.** Mandatory human judgment at: merge,
  architecture change, dependency addition, and release. Everything else is automated.
- **Multi-agent by disjoint scope:** work is decomposed into independent specs with
  non-overlapping module scope; git + small diffs are the concurrency primitive. Overlapping
  changes are resolved at a human gate, never silently merged.
- **Provenance per change:** record which spec, which method/skill, and which agent/model
  produced a change, without polluting app code.
- **Docs are tested:** where a doc makes a checkable claim (a command, a contract), CI checks
  it. Rotten docs are broken builds.

## Governance

This constitution supersedes ad-hoc practice. All changes — by humans or agents — must comply,
and reviews must verify compliance. Amendments require: a documented rationale (an ADR),
approval at the merge gate, and propagation to any dependent templates or specs. Complexity
must be justified against these principles; unjustified complexity is rejected. When this
constitution and a tool's default behavior conflict, the constitution wins and the adapter is
adjusted. Runtime engineering guidance for agents lives in `docs/` and `decisions/`.

**Version**: 1.0.0 | **Ratified**: 2026-08-07 | **Last Amended**: 2026-08-07
