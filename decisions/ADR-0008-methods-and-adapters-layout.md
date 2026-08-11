# ADR-0008: Materialize the neutral `methods/` + tool `adapters/` layout

- **Status:** Accepted
- **Date:** 2026-08-10
- **Deciders:** Project maintainer
- **Related:** [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [ADR-0002 (Spec Kit)](ADR-0002-adopt-spec-kit-as-sdd-engine.md), [ADR-0007 (guardrails)](ADR-0007-quality-gates-detekt-and-method-guardrails.md), [constitution](../docs/constitution.md), [Vision §9.1a](../docs/00-vision-and-architecture.md)
- **Makes real:** the `methods/` and `adapters/` nodes that were **dangling (grey)** in [index.md](../index.md).

---

## Context

ADR-0001 declared the portability guarantee — *the project's intelligence lives in a neutral core;
the AI tool is a thin, replaceable adapter* — but the **layout was never materialized**. Tool coupling
today lives implicitly in `.claude/` (skills, hooks, settings) and `.specify/` (Spec Kit engine), and
there is **no `methods/`** (the neutral "how") and **no `adapters/`** (the explicit invocation layer).
So the single most important claim of the project is asserted, not demonstrated or checkable.

## Decision

Introduce two top-level directories that make the ADR-0001 boundary explicit and enforceable.

### `methods/` — the neutral "how" (tool-agnostic, in prose)
Each capability the project relies on (the SDD loop stages, plus project-specific ones like recording
a decision or verifying a change) gets a **method** doc describing *what it does and how*, independent
of any tool. **No tool names, no tool mechanics.** This is where a capability's intelligence lives.

### `adapters/<tool>/` — the thin invocation layer
An adapter **realizes** methods for one specific tool. It contains *only* packaging/invocation — which
skill file, which hook, which command — never method logic or project knowledge.
- **`adapters/claude-code/`** — the active adapter. Maps each method to its Claude Code realization
  (`.claude/skills/`, `.claude/settings.json` hooks, `scripts/`, the Spec Kit engine in `.specify/`).
- **`adapters/opencode/`** — the **swap target** (ADR-0002 confirmed opencode as available). Documented
  as *planned*; its existence proves the seam is real: swapping tools means writing this directory, and
  the neutral core (`methods/`, `specs/`, `decisions/`, `docs/`, the app, tests) is untouched.

### Golden rule (enforced, not promised)
- **No method or project knowledge inside an adapter.** If it holds *how/why*, it belongs in the core.
- **No tool-specific mechanism inside the neutral core.** `methods/` especially must name no tool.
- **Exception — describe vs. invoke:** `decisions/` and `docs/` MAY *name* a tool when the decision is
  *about* tooling (ADR-0001/0002/0007 discuss Claude Code and Spec Kit). They **describe**; they never
  **invoke**. `methods/` has no such exception — it stays fully neutral.

### Guardrail
`scripts/check-adapter-boundary.sh` (ADR-0007) is extended: in addition to keeping app/library code
tool-neutral, it asserts **`methods/` contains no tool token** (`claude`, `opencode`, `speckit`,
`SKILL.md`, `.specify`). Tool names are allowed only under `adapters/`, `decisions/`, `docs/`.

## Consequences

### Positive
- The ADR-0001 thesis becomes a **visible, checkable structure** — the biggest "asserted but not shown"
  gap closes.
- Onboarding an agent/human: read `methods/` for the *what*, `adapters/<your-tool>/` for the *how here*.
- The `methods/`/`adapters/` graph nodes go from grey (todo) to solid.

### Negative / costs
- A capability now has two artifacts (neutral method + per-adapter mapping) — mild duplication, the
  price of portability.
- `adapters/opencode/` is a working **ai-paced brain** (a local LLM shipped spec `004` end-to-end); the full human-paced SDD-loop commands remain v3 (see Resulting actions).

### Neutral
- The existing `.claude/` and `.specify/` directories stay where the tools expect them; `adapters/`
  points at them rather than moving them.

## Alternatives considered

1. **Keep everything in `.claude/` / `.specify/`.** Rejected — no visible boundary; the thesis stays
   unprovable and unenforceable.
2. **Build a full working opencode adapter now.** Deferred (v3, ADR-0001) — a documented swap-target
   stub already proves the seam; a full second adapter is a large, separate effort.
3. **Move `.claude/` under `adapters/`.** Rejected — tools expect their config at fixed paths; the
   adapter *references* them instead.

## Resulting actions

- [x] Create `methods/` (README + initial neutral method docs).
- [x] Create `adapters/claude-code/` (active) and `adapters/opencode/` (active — ai-paced brain via a local LLM).
- [x] Extend `scripts/check-adapter-boundary.sh` to enforce `methods/` neutrality.
- [x] Un-grey the `methods/`/`adapters/` nodes in `index.md`.
- [ ] Extract a neutral method for every skill over time (currently: the SDD-loop core + verify-change + write-adr).
- [x] `adapters/opencode/` realizes the **ai-paced brain** with a local model (Ollama / LM Studio) —
      neutrality **proven by execution** for that path (ADR-0009). Full SDD-loop commands remain v3.
