# ADR-0012: LLM runtime observability — adapter-scoped

- **Status:** Accepted
- **Date:** 2026-08-11
- **Deciders:** Project maintainer
- **Related:** [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [ADR-0010 (automated provenance + metrics)](ADR-0010-automated-provenance-and-metrics.md), [ADR-0008 (methods/adapters layout)](ADR-0008-methods-and-adapters-layout.md), [ADR-0009 (run modes)](ADR-0009-run-modes-human-paced-and-ai-paced.md), [constitution (Principles IV, V)](../docs/constitution.md), [Vision §7 (metrics)](../docs/00-vision-and-architecture.md)

---

## Context

Two different things get called "observability", and the project already has one but not the other:

- **Process observability (exists — ADR-0010).** Provenance commit trailers + `scripts/provenance.sh`
  + `scripts/metrics.sh` derive, from git alone, *which spec / method / agent / model* produced each
  change, diff sizes, and the ai-paced share. This measures the **SDD loop** ("measure the method,
  don't assert", Vision §7). It is git-native, tool-neutral, and never touches app code.
- **LLM runtime observability (does not exist).** Per-call **token usage, cost, latency**, prompt→
  response **traces**, tool-call/retry spans, and generation-quality **evals** — the kind surfaced by
  tools like Langfuse / LangSmith / Helicone / Phoenix. Nothing in the repo captures this.

The question this ADR settles is **not** "should we log LLM runtime signals" but **where that
capability is allowed to live**, so the answer is fixed before anyone wires a provider in.

The tension is with the constitution:
- **Principle V (neutral core, pluggable adapters):** raw runtime signals are inherently
  **tool-specific** — Claude Code and opencode expose token/cost/latency/trace data through different,
  provider-shaped surfaces. Putting that capture in the neutral core would couple the project to a
  provider, the exact thing adapters exist to prevent.
- **Principle IV (knowledge in git, with proveniência):** the project's durable, queryable record is
  git. A separate runtime telemetry store is an outside-of-git truth that can drift.

## Decision

**LLM runtime observability is an adapter-scoped concern.** Concretely:

1. **The neutral core carries no LLM-runtime instrumentation.** No token/cost/latency/trace capture in
   `methods/`, `scripts/`, `specs/`, app code, or the constitution's core loop. Process observability
   stays git-native (ADR-0010) and remains the *only* observability in the core.
2. **If/when runtime observability is added, it lives entirely inside an adapter**
   (`adapters/claude-code/` or `adapters/opencode/`) — never in the core. It must obey the adapter
   boundary already enforced by `scripts/check-adapter-boundary.sh`: **no domain/project knowledge in
   the adapter**, and swapping the AI tool must not lose project knowledge.
3. **Adding a runtime-observability sink is a human gate.** Any external sink/SDK is a
   **`dependency-add`** (and, if it changes the run-mode harness contract, an **architecture-change**)
   gate — it requires promoting this ADR's "trigger to revisit" into a follow-up accepted decision, not
   a silent addition.
4. **Only reduced aggregates may cross back into the core**, and only through the existing git-native
   channel: an adapter may summarize runtime signals into provenance/metrics-shaped data (e.g. a
   per-spec token/cost aggregate emitted as a commit trailer or consumed by `scripts/metrics.sh`), so
   the core keeps a tool-neutral, git-native view without importing raw provider telemetry.

This deliberately mirrors [the fine-tuning discussion]: both fine-tuning and runtime telemetry are
**adapter-level** concerns subordinate to Principles IV and V; the core's lever is **context in git**,
not model weights or provider telemetry.

## Consequences

### Positive
- The core stays **neutral and provider-agnostic** (Principle V); swapping brains never entangles a
  telemetry provider.
- One source of truth for anything durable — **git** (Principle IV); no parallel telemetry store to
  drift against the ledger.
- The boundary is decided **before** implementation, so a future integration has an unambiguous home
  and a clear gate.

### Negative / costs
- Today there is **no** per-call cost/latency/quality visibility for agent runs; only git-derived
  process metrics exist. Cost/efficiency of a run is not directly measurable yet.
- Cross-adapter comparison (Claude Code vs opencode on the same spec) is limited to what git-native
  aggregates can express until an adapter implements richer capture.

### Neutral
- This ADR sets **policy/placement**, not a tool choice. It neither adds nor forbids a specific
  observability product; it constrains *where* one may go.

## Trigger to revisit

Promote to a follow-up accepted decision (which adds the sink + version-catalog/deps via the human
`dependency-add` gate) when **either** holds:

1. Autonomous **ai-paced** runs become frequent enough that **cost/latency/quality per run** must be
   managed, not estimated; **or**
2. A concrete need to **compare adapters/models** quantitatively (cost or quality per spec) arises.

Preferred shape at that point (recorded to save future work, not decided here):
- **Placement:** inside the relevant `adapters/<tool>/`, emitting to the chosen sink; the core only ever
  ingests reduced, git-native aggregates.
- **Boundary check:** `scripts/check-adapter-boundary.sh` must still pass (no domain knowledge leaks
  into the adapter).
- **Privacy:** prompt/response logging may contain repo content — treat as sensitive; opt-in, and never
  a default that ships in the neutral core.

## Alternatives considered

1. **Instrument the neutral core / run-mode harness directly.** Rejected — couples the core to a
   provider's telemetry surface; violates Principle V.
2. **A separate telemetry store as a second source of truth** (e.g. a hosted tracing backend treated as
   canonical). Rejected as canonical — duplicates/drifts from git (Principle IV). Allowed only as an
   adapter-side sink whose *aggregates* reduce back into git.
3. **Do nothing / leave it undefined.** Rejected — without a decided boundary, the first integration
   would likely land in the core by expedience and quietly break neutrality.

## Resulting actions

- [x] Record the placement decision (this ADR); link from `index.md` Knowledge Map and CLAUDE.md.
- [ ] When the trigger fires: follow-up ADR to add a specific adapter-scoped sink (human
      `dependency-add` gate), defining the git-native aggregate contract and privacy stance.
- [ ] (Optional) Extend `scripts/metrics.sh` to accept adapter-supplied aggregates (e.g. a
      `Provenance-Cost`/token trailer) if/when an adapter starts emitting them — keeps the core view
      git-native.
