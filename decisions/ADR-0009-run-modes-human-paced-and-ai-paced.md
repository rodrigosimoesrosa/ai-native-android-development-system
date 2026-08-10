# ADR-0009: Two run modes (human-paced / ai-paced) as one orchestration axis

- **Status:** Accepted
- **Date:** 2026-08-10
- **Deciders:** Project maintainer
- **Related:** [ADR-0001 (neutral core)](ADR-0001-build-on-existing-tools-neutral-core.md), [ADR-0008 (methods/adapters)](ADR-0008-methods-and-adapters-layout.md), [ADR-0007 (gates/verify)](ADR-0007-quality-gates-detekt-and-method-guardrails.md), [constitution](../docs/constitution.md), [Vision (roadmap)](../docs/00-vision-and-architecture.md), [`methods/sdd-loop`](../methods/sdd-loop.md), [`methods/verify-change`](../methods/verify-change.md)

---

## Context

The repository should offer **two ways to run the same loop**:
1. **human-paced** — a person advances the loop turn by turn (today's default).
2. **ai-paced** — an agent advances the loop autonomously (triggered from an open spec) until the
   verification gate is green and a pull request is opened.

The risk is treating these as two systems and forking the codebase/method into divergent halves. The
`methods/` + `adapters/` split (ADR-0008) lets us avoid that: a run mode is a **thin orchestration
axis**, not a fork.

## Decision

**Both modes share the entire neutral core** — `methods/` (the loop), the verification gates of
`methods/verify-change` (tests + static analysis + guardrails), the Android app, `specs/`, `decisions/`.
They differ on exactly two things:

1. **Driver** — who advances each loop step: a human (interactive) or an agent (autonomous).
2. **Gate policy** — which gates require a human, declared in [`run-modes.yml`](../run-modes.yml).

### Gate policy (the safety control)
The constitution's mandatory human gates — **merge, architecture change, dependency addition,
release** — stay **human in both modes**. `ai-paced` is therefore **not "no humans"**: it is autonomous
only over *small, verifiable units within an already-approved spec*, and **escalates** the four gates
to a person. It produces a PR; a human reviews and merges.

### Realization (per adapter, ADR-0008)
- **human-paced** — an interactive session (exists today).
- **ai-paced** — a **headless runner** (an automation-triggered command) that closes the loop and opens
  a PR. Delivered first as a scoped stub, later hardened (bounded self-correction, gate escalation,
  auto-provenance). This is the same capability the vision calls the "cold-start self-sufficiency"
  proof; here it is promoted to a **first-class run mode**.

### Provenance
Both modes record the same provenance schema (spec, method, agent/model); `ai-paced` captures it
automatically in the runner.

## Consequences

### Positive
- Two modes from **one** core — no fork, no divergence. The mode is configuration + a driver.
- `ai-paced` is **safe by construction**: identical fitness function (gates) + mandatory human gates
  preserved. It cannot merge, add a dependency, or change architecture on its own.
- Makes the "AI-first loop" a **configurable, first-class capability**, not a one-off script.

### Negative / costs
- The robust `ai-paced` runner (autonomous agent, retries, escalation) is the hard part — deferred;
  only the design + config + a stub land now.
- One more config (`run-modes.yml`) to keep aligned with the constitution's gates.

### Neutral
- `human-paced` is unchanged; this ADR only names and formalizes what already happens, and adds the
  second mode alongside it.

## Alternatives considered

1. **Fork into two systems** (a "manual" repo and an "autonomous" repo). Rejected — divergence and
   double maintenance; defeats the single-source-of-truth principle.
2. **ai-paced with full autonomy (no human gates).** Rejected — violates the constitution's mandatory
   human gates; unsafe for dependency/architecture/release.
3. **Only human-paced.** Rejected — misses the project's headline thesis (a repo an agent can operate).

## Resulting actions

- [x] `methods/run-modes.md` — the neutral description of the mode axis.
- [x] `run-modes.yml` — per-mode gate policy + autonomous stop condition.
- [x] Record this ADR and link it in `index.md`.
- [x] `scripts/ai-paced-run.sh`: **tool-neutral** `ai-paced` harness — reads the policy, escalates the
      mandatory gates, runs the shared verification gate in a bounded loop, and **closes the loop with a
      pluggable agent** (`AI_PACED_AGENT_CMD`). Demonstrated end-to-end with a mock (RED → fix → GREEN →
      open change request); contract in `adapters/claude-code/agents/README.md`.
- [x] Per-adapter brains: `adapters/claude-code/run-ai-paced.sh` (Claude Code) and
      `adapters/opencode/run-ai-paced.sh` (**opencode → local Ollama / LM Studio model**) — swap the
      brain by choosing the launcher; harness/gate/escalation/provenance unchanged.
- [x] Auto-provenance capture in the runner (exports `PROVENANCE_*`, ADR-0010).
- [ ] Run with a **capable** model end-to-end on a real spec (the full `#3` — a small local model may
      not converge; a strong model closes it). Model capability is now the only variable.

## Amendments

### Amendment 1 — ai-paced is the *executor of an approved plan* (2026-08-10)

**Refinement of the definition.** ai-paced is not a free-floating "fix-the-gate" loop; it is the
**autonomous executor of an approved `tasks.md`**. The handoff boundary is the **approval of the task
breakdown**: the specification, plan, and tasks are the high-leverage reasoning a human (or a strong
model, in human-paced) owns and approves; from that approval, ai-paced executes — take the next
unchecked task → implement → run the gate → on green mark it `[x]` → repeat; open a change request when
the list is complete and green. It never invents scope.

**Why:** this ties ai-paced to `spec/plan/tasks` (the approved `tasks.md` is the **contract** between a
strong planner and a cheap executor — the planner/executor split), matches the constitution ("code
implements a spec"), and makes a weaker/local executor viable (narrow units + the gate as fitness).

**Encoded in:** `methods/run-modes.md` (Handoff boundary), `run-modes.yml` (ai-paced `input`), and
`scripts/ai-paced-run.sh` (now **requires** an approved `tasks.md`, refuses without one, and drives the
brain task-by-task with `TASKS`/`NEXT_TASK`). The brains (`adapters/*/run-ai-paced.sh`) prompt the agent
to implement `NEXT_TASK` and mark it `[x]`.
