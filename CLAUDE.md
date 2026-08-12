# CLAUDE.md

Guidance for any AI agent (Claude Code, opencode, …) working in this repo. All project
knowledge lives as versioned markdown in git (Constitution Principle IV) — read it on demand;
nothing here is optional context.

## Read first (entry point)

- [`index.md`](index.md) — the Knowledge Map (mermaid graph + links to everything below).
- [`docs/00-vision-and-architecture.md`](docs/00-vision-and-architecture.md) — what this project is and why.
- [`docs/constitution.md`](docs/constitution.md) — **the rules humans and agents build by** (v1.1.0). Non-negotiable.

## Core principles (the constitution, condensed — canonical: `.specify/memory/constitution.md`)

- **I. Specs are the source of truth (NON-NEGOTIABLE).** No implementation without an approved
  spec. Code implements a spec, tests verify it; if they disagree, one is a bug — fix it.
- **II. Small, verifiable units with explicit boundaries (SOLID).** Many small reviewable diffs,
  each mapped to a scoped spec. **Dependency Inversion** — arrows point inward toward the domain
  (ADR-0003).
- **III. Tests as executable specification (NON-NEGOTIABLE).** Acceptance criteria as tests,
  test-first. "Done" = the verification gate passes. Green is the default; red is broken work.
- **IV. Knowledge in git, with provenance.** Specs, ADRs, contracts, conventions are versioned,
  cross-linked files reproducible by `git clone`. Every change traces to its spec + decision record.
- **V. Neutral core, tools as pluggable adapters.** Project intelligence lives in tool-agnostic
  files (ADR-0001); AI tooling is a thin, replaceable adapter with no domain logic inside it.

## Architecture is decided, not re-decided per feature

Implement **within** the recorded decisions in [`decisions/`](decisions/). If a task conflicts
with an ADR, **stop and escalate** — do not invent a new architectural decision.

- [ADR-0001](decisions/ADR-0001-build-on-existing-tools-neutral-core.md) — Neutral core, tools as pluggable adapters.
- [ADR-0002](decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md) — Spec Kit as the SDD engine.
- [ADR-0003](decisions/ADR-0003-android-architecture-clean-mvi.md) — Android architecture: **Clean Architecture + MVI**.
- [ADR-0004](decisions/ADR-0004-dependency-injection-hilt.md) — Dependency Injection with **Hilt**.
- [ADR-0005](decisions/ADR-0005-local-persistence-room-datastore.md) — Local persistence: **Room + Proto DataStore**.
- [ADR-0006](decisions/ADR-0006-networking-and-auth-token-strategy.md) — Networking + JWT auth/refresh strategy.
- [ADR-0007](decisions/ADR-0007-quality-gates-detekt-and-method-guardrails.md) — Quality gates: **detekt** + method guardrails.
- [ADR-0008](decisions/ADR-0008-methods-and-adapters-layout.md) — `methods/` + `adapters/` layout.
- [ADR-0009](decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md) — Run modes: human-paced / ai-paced.
- [ADR-0010](decisions/ADR-0010-automated-provenance-and-metrics.md) — Automated provenance (commit trailers) + metrics.
- [ADR-0011](decisions/ADR-0011-visual-regression-testing-deferred.md) — Visual-regression (snapshot) testing: **Deferred**.
- [ADR-0012](decisions/ADR-0012-llm-runtime-observability-adapter-scoped.md) — LLM runtime observability (token/cost/latency/traces): **adapter-scoped**, never in the neutral core.
- [ADR-0013](decisions/ADR-0013-per-commit-token-cost-aggregate.md) — Per-commit token/cost aggregate via provenance trailers: **Proposed** (per-commit, not per-file).
- [ADR-0014](decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md) — LLM diagnostic telemetry (traces/logs/evals): **Proposed**, adapter-scoped; content never crosses to git.

## How the work is done (the neutral "how")

- [`methods/sdd-loop.md`](methods/sdd-loop.md) — the loop: `spec → plan → break down → implement → verify → record`.
- [`methods/verify-change.md`](methods/verify-change.md) — the definition of "done": a change is done only when checks pass.
- [`methods/run-modes.md`](methods/run-modes.md) + [`run-modes.yml`](run-modes.yml) — human-paced vs **ai-paced** driver + gate policy.
- [`scripts/ai-paced-run.sh`](scripts/ai-paced-run.sh) + [`adapters/`](adapters/README.md) — the tool-neutral ai-paced harness with a **pluggable brain**: [claude-code](adapters/claude-code/README.md) and [opencode](adapters/opencode/README.md) (a **local** LLM) are **both active** — opencode drove spec `004` end-to-end.
- [`methods/write-adr.md`](methods/write-adr.md) — how to record a decision.
- [`methods/record-provenance.md`](methods/record-provenance.md) — provenance trailers (Principle IV / ADR-0010).
- **SDD-loop commands** exist for both brains under the same `/speckit-*` names: Claude Code skills
  in [`.claude/skills/`](.claude/skills/) and opencode commands in [`.opencode/command/`](.opencode/command/).

## Human gates (never crossed autonomously)

From the constitution and `run-modes.yml`, these **always** require a human — an agent must
stop and escalate, never self-approve:

- **merge** — a person reviews and merges every change. Do not push or open a PR without approval.
- **architecture-change** — see ADRs above.
- **dependency-add** — do not add a third-party dependency that is not already in the version catalog.
- **release**.

## Working rules

- **No implementation without an approved spec.** Specs live in [`specs/`](specs/); implement only an approved `specs/<feature>/tasks.md`.
- **Tests are the fitness function** (Principle III, NON-NEGOTIABLE): acceptance criteria as tests, test-first. Green is the default state; red is broken work.
- **Reuse, don't rebuild** — each spec's `tasks.md` names its reuse targets from prior specs.
- **Prefer many small, reviewable changes** mapped to a scoped spec.
- **Package base:** `com.mirabilis`. Quality gate: **detekt** (config in [`config/detekt/`](config/detekt/)).
- **Commit trailers** carry provenance (spec, method, agent/model) per ADR-0010.

## Note on Obsidian

`.obsidian/` is only the Obsidian app config (viewer settings) — **not** a knowledge source.
The knowledge is the markdown in `decisions/`, `methods/`, `docs/`, and `index.md`. Obsidian
just renders the same files as a graph; ignore `.obsidian/` when gathering context.