# ADR-0007: Quality gates — static analysis (detekt) + method guardrails in CI

- **Status:** Accepted
- **Date:** 2026-08-10
- **Deciders:** Project maintainer
- **Related:** [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [ADR-0002 (Spec Kit)](ADR-0002-adopt-spec-kit-as-sdd-engine.md), [constitution](../docs/constitution.md), [Vision & Architecture](../docs/00-vision-and-architecture.md)
- **Closes:** vision open-questions **#6** (cold-start self-sufficiency check) and **#7** (neutral-core/adapter CI guardrail).

---

## Context

Until now the only automated gate was the unit-test suite. The constitution and vision ask for more:
"green CI is the default state", "**docs are tested** — a doc that makes a checkable claim is checked
by CI", the **cold-start agent self-sufficiency** check (§7/open-Q#6), and the **neutral-core/adapter
boundary** guardrail (ADR-0001/open-Q#7). We also deferred a real lint tool at skeleton time (T061).

## Decision

Adopt three automated quality gates, wired into CI, all boring and deterministic.

### 1. Static analysis — detekt
- **detekt `1.23.7`** applied to every module from the root build (one shared config
  `config/detekt/detekt.yml`, `buildUponDefaultConfig = true`), including the **`detekt-formatting`**
  ruleset (ktlint rules) which reads `.editorconfig`. Run with `./gradlew detekt`; `--auto-correct`
  fixes formatting locally, CI verifies only.
- Intentional broad catches at the `Safe*DataSource` error boundary (ADR-0003) are `@Suppress`ed with
  justification, not by weakening the rule globally.

### 2. Knowledge / cold-start-lite guardrail — `scripts/check-knowledge.sh`
Verifies the knowledge layer is present (README, index, vision, constitution, ADR-0001; and every
`specs/*/` has `spec.md`+`plan.md`+`tasks.md`) and that **all internal markdown links resolve**. A
broken link is a broken build. This is the cheap, deterministic proxy for "an agent given only the
repo has complete, non-broken context" — a full agent-driven cold-start check is deferred (§6).

### 3. Neutral-core/adapter boundary guardrail — `scripts/check-adapter-boundary.sh`
Asserts the Android app/library Kotlin+Gradle sources contain **zero references to the AI tooling**
(`claude`, `speckit`, `.specify`, `SKILL.md`, `adapters/`, `anthropic`). The project's intelligence
stays tool-neutral; tool coupling lives only in the disposable adapter (`.claude/`, `.specify/`).

### CI wiring
`.github/workflows/ci.yml`: a `guardrails` job (fast, no Gradle) runs both scripts, then a `build`
job runs `./gradlew test` + `./gradlew detekt`. The local `Stop` hook keeps running the fast JVM
tests for in-session feedback (unchanged).

## Consequences

### Positive
- The *method* is now enforced, not just asserted — closes two v1 exit-criteria items.
- One lint tool (detekt) covers static analysis **and** formatting; reads the existing `.editorconfig`.
- Guardrails are plain bash + git — reproducible by `git clone`, no extra runtime.

### Negative / costs
- detekt `1.23.x` bundles an older Kotlin analyzer; it runs **without type resolution** here (fine for
  our rule set) and may log a Kotlin-version notice. A new dev dependency was added — **this ADR is the
  dependency-addition gate** (constitution).
- The cold-start check is a proxy (presence + links), not a real agent run.

### Neutral
- The boundary guardrail is trivially satisfied today; its value is catching *future* coupling.

## Alternatives considered

1. **ktlint standalone.** Rejected — `detekt-formatting` already wraps ktlint and adds static analysis.
2. **A real cold-start agent in CI** (spin up an agent, hand it the repo, assert it completes a spec).
   Deferred — high cost/flakiness; the presence+link check is the deterministic v1 version.
3. **A knowledge-graph database** to validate the graph. Rejected per vision §6 (over-engineering).
4. **Baseline the existing detekt findings.** Rejected — the codebase is small; we fixed them instead.

## Resulting actions

- [x] Pin detekt `1.23.7` + `detekt-formatting`; apply from root build; add `config/detekt/detekt.yml`.
- [x] `scripts/check-knowledge.sh` and `scripts/check-adapter-boundary.sh`.
- [x] CI runs guardrails → tests → detekt.
- [ ] Extend the boundary guardrail as an explicit `adapters/` directory materializes (v3, ADR-0001).
- [ ] Evolve the cold-start check toward an actual agent run when practical.
