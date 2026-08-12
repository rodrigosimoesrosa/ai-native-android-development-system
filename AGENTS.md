# AGENTS.md

Entry point for **opencode** (and any AGENTS.md-aware agent). opencode auto-loads this file every
session — the same role `CLAUDE.md` plays for Claude Code. Because a weaker/local model may not
follow pointers to other files, the **durable guardrails are inlined below** (not just linked), so
they are always in context. The **canonical** sources remain authoritative and win on any conflict;
when a task touches one, open the full file:

- Constitution: `.specify/memory/constitution.md` (v1.1.0) — `docs/constitution.md` is only a pointer.
- Decisions: [`decisions/`](decisions/) (ADRs). Methods: [`methods/`](methods/). Map: [`index.md`](index.md).

Keep this file in sync when those sources change (it is a condensed mirror — drift is the one cost of
inlining; the canonical files are the truth).

## Core principles (from the constitution)

- **I. Specs are the source of truth (NON-NEGOTIABLE).** No implementation starts without an approved
  spec. Code implements a spec; tests verify it. If code and spec disagree, one is a bug — fix it.
- **II. Small, verifiable units with explicit boundaries (SOLID).** Many small reviewable diffs, each
  mapped to a scoped spec with machine-checkable criteria. **Dependency Inversion**: dependency arrows
  point inward toward the domain (ADR-0003).
- **III. Tests as executable specification (NON-NEGOTIABLE).** Acceptance criteria as tests, test-first.
  A change is "done" only when its tests pass. Green is the default; red is broken work.
- **IV. Knowledge in git, with provenance.** Specs, ADRs, contracts, conventions live as versioned,
  cross-linked files — reproducible by `git clone`. Every change traces to its spec + decision record.
- **V. Neutral core, tools as pluggable adapters.** Project intelligence lives in tool-agnostic files
  (ADR-0001). AI tooling (Spec Kit, Claude Code, opencode) is a thin, replaceable adapter — no domain
  logic or project knowledge inside an adapter. Favor boring, mainstream technology.

## Four human gates — never crossed autonomously

An agent must **stop and escalate**, never self-approve:

- **merge** — a person reviews and merges every change. Do not push or open a PR without approval.
- **architecture-change** — implement *within* the ADRs; a conflicting task is an escalation.
- **dependency-add** — do not add a dependency that is not already in the version catalog.
- **release**.

## Architecture is decided in ADRs (implement within them)

- [ADR-0001](decisions/ADR-0001-build-on-existing-tools-neutral-core.md) — Neutral core, tools as adapters.
- [ADR-0002](decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md) — Spec Kit as the SDD engine.
- [ADR-0003](decisions/ADR-0003-android-architecture-clean-mvi.md) — Clean Architecture + MVI.
- [ADR-0004](decisions/ADR-0004-dependency-injection-hilt.md) — DI with Hilt.
- [ADR-0005](decisions/ADR-0005-local-persistence-room-datastore.md) — Room + Proto DataStore.
- [ADR-0006](decisions/ADR-0006-networking-and-auth-token-strategy.md) — Networking + JWT auth/refresh.
- [ADR-0007](decisions/ADR-0007-quality-gates-detekt-and-method-guardrails.md) — Quality gates: detekt + guardrails.
- [ADR-0008](decisions/ADR-0008-methods-and-adapters-layout.md) — `methods/` + `adapters/` layout.
- [ADR-0009](decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md) — Run modes: human-paced / ai-paced.
- [ADR-0010](decisions/ADR-0010-automated-provenance-and-metrics.md) — Automated provenance + metrics.
- [ADR-0011](decisions/ADR-0011-visual-regression-testing-deferred.md) — Visual-regression testing: deferred.
- [ADR-0012](decisions/ADR-0012-llm-runtime-observability-adapter-scoped.md) — LLM runtime observability: adapter-scoped.
- [ADR-0013](decisions/ADR-0013-per-commit-token-cost-aggregate.md) — Per-commit token/cost aggregate: proposed.
- [ADR-0014](decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md) — LLM diagnostic telemetry: proposed, adapter-scoped.

## The loop & definition of done

- **The one loop** (`methods/sdd-loop.md`): `spec → plan → break down → implement → verify → record`.
- **Done** (`methods/verify-change.md`): a change is done only when the verification gate passes
  (guardrails + tests green). Not before.
- **Run modes** (`methods/run-modes.md` + `run-modes.yml`): human-paced (person advances turn by turn)
  vs ai-paced (agent executes an already-approved `tasks.md` to completion, then opens a change
  request). The four gates stay human in both.
- **Provenance** (`methods/record-provenance.md`): commit trailers record spec / method / agent / model
  (ADR-0010) — metadata only, never in app code.

## Working rules

- **No implementation without an approved spec.** Specs live in [`specs/`](specs/); implement only an
  approved `specs/<feature>/tasks.md`, marking each completed task `[x]`.
- **Reuse, don't rebuild** — each `tasks.md` names its reuse targets from prior specs.
- **Prefer many small, reviewable changes** mapped to a scoped spec.
- **Package base:** `com.mirabilis`. **Quality gate:** detekt (`config/detekt/`).
- Do **not** commit autonomously (merge is a human gate).

## Spec Kit commands (opencode)

The `/speckit-*` commands mirror the Claude Code skills and are defined in
[`.opencode/command/`](.opencode/command/). Each references its authoritative spec in
`.claude/skills/speckit-*/SKILL.md` (single source of truth — no duplication).
