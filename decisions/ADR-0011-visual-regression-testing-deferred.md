# ADR-0011: Visual-regression (snapshot) testing — deferred

- **Status:** Deferred
- **Date:** 2026-08-11
- **Deciders:** Project maintainer
- **Related:** [ADR-0007 (quality gates)](ADR-0007-quality-gates-detekt-and-method-guardrails.md), [ADR-0003 (Clean Architecture + MVI)](ADR-0003-android-architecture-clean-mvi.md), [ADR-0009 (run modes)](ADR-0009-run-modes-human-paced-and-ai-paced.md), [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [constitution](../docs/constitution.md), [Vision & Architecture](../docs/00-vision-and-architecture.md)

---

## Context

The verification gate (ADR-0007) covers static analysis (detekt), the knowledge/boundary guardrails,
and JVM unit tests. The fast in-session loop (the `Stop` hook and the ai-paced harness) runs only
`:core:test` and `:domain:test` — so the **presentation layer** (`core-ui`, `feature/*` Compose UI) is
the layer least covered by the gate that fires on every turn.

There is a thesis-level argument for visual-regression (snapshot) testing here: an AI agent **cannot see
the UI it generates**. A screenshot golden is a machine-checkable fitness function for *visual* output —
directly in the spirit of constitution Principle III ("tests are the fitness function") and of the
AI-native run modes (ADR-0009), where an autonomous brain may author Compose UI it can never eyeball.

## Decision

**Defer** adding visual-regression/snapshot testing to the quality gate **at this time**. Do not add a
snapshot-testing dependency or a new gate step now.

Rationale — the cost lands before the benefit at the project's current stage:

- **Non-determinism vs. "red = broken work".** Snapshot tests go red for rendering drift (font metrics,
  layoutlib/JDK/library version differences), not only real regressions. Under the constitution's
  "green is the default; red is broken work" rule, false-red erodes trust in the gate. This is the
  strongest reason to keep it out of the *blocking* gate until it is warranted.
- **Golden-image maintenance.** Every intentional UI change forces regenerating and reviewing binary
  golden images in git — recurring cost that only pays off with meaningful, changing UI surface.
- **Stage fit.** The UI surface is still small (specs `001`–`004`) and the active frontier is the
  **methodology / workflow**, not visual polish. Paying a flaky, maintenance-heavy cost now buys little.

## Consequences

### Positive
- The gate stays boring and deterministic (ADR-0007) — no new flaky surface, no golden churn.
- No new dependency is added; `dependency-add` (a human gate) is not triggered by this ADR.

### Negative / costs
- Visual regressions in `core-ui` / `feature/*` are **not** caught automatically; a person reviewing the
  UI is the only backstop until this is revisited.
- The "agent without eyes" blind spot for autonomously authored Compose UI remains open (accepted for now).

### Neutral
- This is a decision to *defer*, not to reject: it is expected to be promoted to an accepted decision
  when the trigger below fires.

## Trigger to revisit

Promote this to a full, accepted decision (add the tool + version-catalog entry — still a human
`dependency-add` gate) when **either** holds:

1. An **ai-paced run authors non-trivial Compose UI end-to-end** — the "agent without eyes" becomes a
   real, recurring risk rather than a hypothetical; **or**
2. The UI surface grows enough that visual regressions are a genuine, recurring maintenance cost.

Preferred shape at that point (not decided here, recorded to save future work):

- **Tool:** Paparazzi (JVM / layoutlib, no emulator) — fits the existing "fast JVM" gate model far
  better than Robolectric-based or instrumented screenshot tests.
- **Placement:** its own CI gate (record vs. verify), **not** the fast in-session `Stop`-hook loop —
  golden comparison must not block every turn or slow the session loop.
- **Flake control:** goldens versioned in git; layoutlib/JDK pinned; a golden update is a human-reviewed
  change, never auto-approved by the agent.

## Alternatives considered

1. **Add Paparazzi now.** Rejected for now — cost (flaky red, golden maintenance) arrives before the
   benefit while the UI surface is small; false-red would erode gate trust.
2. **Roborazzi (Robolectric-based).** Deferred with the rest; heavier runtime than Paparazzi's JVM path.
3. **AGP Compose Preview Screenshot Testing.** Rejected — alpha maturity conflicts with ADR-0007's
   "boring and deterministic" value for gates.
4. **Close the near-term gap more cheaply:** bring `:core-ui` / `:feature` JVM unit tests into the fast
   in-session gate (which today runs only `:core` / `:domain`). Noted as the higher-value near-term move,
   independent of snapshot testing.

## Resulting actions

- [ ] Revisit when the trigger fires; promote to an accepted decision (Paparazzi + catalog entry via the
      human `dependency-add` gate).
- [ ] (Optional, near-term) Consider extending the fast gate to include `:core-ui` / `:feature` JVM tests
      — a separate, cheaper coverage win that does not depend on this ADR.