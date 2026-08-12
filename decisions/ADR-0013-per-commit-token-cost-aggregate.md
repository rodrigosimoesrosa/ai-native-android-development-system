# ADR-0013: Per-commit token/cost aggregate via provenance trailers

- **Status:** Proposed (contract decided; activation gated — see §Activation)
- **Date:** 2026-08-11
- **Deciders:** Project maintainer
- **Related:** [ADR-0012 (LLM runtime observability — adapter-scoped)](ADR-0012-llm-runtime-observability-adapter-scoped.md) *(this refines it)*, [ADR-0010 (automated provenance + metrics)](ADR-0010-automated-provenance-and-metrics.md) *(reuses its trailer + hook mechanism)*, [ADR-0009 (run modes)](ADR-0009-run-modes-human-paced-and-ai-paced.md), [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [constitution (Principles IV, V)](../docs/constitution.md), [Vision §7 (metrics)](../docs/00-vision-and-architecture.md)

---

## Context

ADR-0012 fixed *where* LLM runtime signals may live (inside an adapter) and stated that only **reduced,
git-native aggregates** may cross back into the neutral core. It named a `Provenance-Cost`/token trailer
as the intended channel but did **not** specify the contract. This ADR specifies that contract so an
adapter and `scripts/metrics.sh` can agree on a schema before anyone captures numbers.

The signal we want: **how much did producing this change cost in tokens (and money)?** — computed the
same way as the rest of provenance (ADR-0010): git-native, queryable, never in app code.

Two forces shape the contract:
- **Provider neutrality (Principle V).** The project runs both a cloud brain (claude-code) and a
  **local** brain (opencode). The schema must represent both without lying — notably, local inference
  has **no API bill** but is **not costless**.
- **"Measure, don't assert" (Vision §7) + provenance's `-` convention (ADR-0010).** A missing/unknown
  value must be distinguishable from a measured zero.

## Decision

### 1. Trailer schema (git commit metadata only)

Extend the ADR-0010 trailer block with two optional keys:

```
Provenance-Tokens: 48213      # integer; total tokens the model processed for this commit's work
Provenance-Cost:   0.72USD    # external API billing cost; "<amount><ISO-4217>", or 0USD, or -
```

Optional finer keys (an adapter MAY emit; `metrics.sh` MUST tolerate their absence):

```
Provenance-Tokens-In:  31002  # prompt/input tokens
Provenance-Tokens-Out: 17211  # completion/output tokens
```

`Provenance-Tokens` remains the authoritative total; if both the split and the total are present, the
total wins for aggregation (do not sum in+out on top of it — avoids double counting).

### 2. Value semantics (the `0` vs `-` rule)

- **`-` (or absent) = unknown / not measured.** Used when the runtime does not expose the number.
  `metrics.sh` skips it (same rule it already applies to `Provenance-Method`).
- **`0` = measured zero.** A real, observed zero.
- **`Provenance-Cost` is defined as external API billing cost.** Therefore:
  - **Cloud (claude-code):** the real billed cost, e.g. `0.72USD`.
  - **Local (opencode):** **`0USD`** — there is no API invoice. This is honest *because Cost is defined
    as billing cost*, not total resource cost. If the adapter cannot even assert that, use `-`.
- **`Provenance-Tokens` is the provider-neutral work metric** and is the field to compare across
  adapters/models — **not** `Cost`. Local runs still carry a real token count if the runtime reports it
  (e.g. ollama/llama.cpp `prompt_eval_count` + `eval_count`); if not, `-`.

### 3. Tokenizer caveat (recorded, not enforced)

Token counts from different models use different tokenizers, so cross-model `Provenance-Tokens` is a
**trend / order-of-magnitude** comparison, not a 1:1 equivalence. `metrics.sh` output must label
per-model token sums as such; it must never present cross-model token totals as directly equal.

### 4. Granularity: per-commit only

- The aggregate belongs to the **commit** (it is a commit trailer). **Per-file token attribution is
  explicitly rejected** — a single generation spans planning, multi-file reads, tool calls, and N file
  edits; there is no measured mapping from tokens to an individual file, and rationing by files-touched
  would be a fabricated metric (violates "measure, don't assert").

### 5. Session → commit attribution (no double counting)

Runtime cost is naturally **per agent session**, and a session may yield several commits. Rule:

- The adapter attributes a session's token/cost total to **exactly one commit** — the session's final
  commit — and stamps any other commits from the same session with `-` for these keys. This guarantees
  `sum(Provenance-Tokens)` over history never double-counts a session.
- Preferred simplification where feasible: the ai-paced harness commits **once per session**, making
  the mapping 1:1.

### 6. Capture & stamping (reuses ADR-0010 plumbing)

- The **adapter** (inside `adapters/<tool>/`, per ADR-0012) reads token/cost from its runtime and
  exports `PROVENANCE_TOKENS` / `PROVENANCE_COST` (and optionally the `_IN`/`_OUT` split) as environment
  variables **before committing**, exactly like the existing `PROVENANCE_*` exports (ADR-0010 §4).
- `githooks/prepare-commit-msg` appends any present `Provenance-Tokens`/`Provenance-Cost` trailers;
  unset ⇒ the keys are simply omitted (never `0`). No app code changes.

### 7. Aggregation (`scripts/metrics.sh`)

Extend the read-only metrics view to sum tokens and cost per spec / agent / model (reusing the existing
`trailers:key=…,valueonly` pipeline), skipping `-`, honoring the total-wins rule from §1, and labeling
per-model token sums with the tokenizer caveat from §3.

## Consequences

### Positive
- **Cost/efficiency of a run becomes measurable per commit**, git-native and queryable, with no second
  source of truth (Principle IV) and no app-code pollution.
- **Honest across cloud and local**: `Tokens` compares work; `Cost` is billing cost (0 for local by
  definition, not a fudge); `-` cleanly means "unknown".
- Reuses ADR-0010's hook + trailer machinery — minimal new surface in the core.

### Negative / costs
- Requires an adapter that can actually read token/cost from its runtime (the gated part — §Activation).
- `Cost=0USD` for local understates *real* resource use (energy/hardware); mitigated by defining Cost as
  billing cost and steering comparison to `Tokens`. A future ADR could add a non-monetary local metric
  (GPU-seconds/energy) if needed — out of scope here.
- Cross-model token sums are only approximate (tokenizer differences); mitigated by labeling, not by
  normalization.

### Neutral
- This ADR decides the **contract/schema** (git-native, no dependency). Whether reading tokens from a
  given runtime needs a new dependency is determined at activation and, if so, passes the human
  `dependency-add` gate (ADR-0012 §3).
- **Privacy:** token *counts* and cost carry **no prompt/response content**, so they are safe in git
  metadata — unlike raw prompt/response logging, which ADR-0012 keeps opt-in and adapter-side.

## Activation

This contract activates when ADR-0012's trigger fires (autonomous runs frequent enough to manage
cost/latency/quality, or a concrete need to compare adapters/models). Until then it is a **specified,
dormant schema**: nothing emits these trailers, and `metrics.sh` need not yet read them.

## Alternatives considered

1. **Separate `tokens.jsonl` / external telemetry as canonical.** Rejected — second source of truth,
   drift risk (Principle IV); ADR-0012 already confines external sinks to adapters with only aggregates
   crossing back.
2. **Per-file attribution.** Rejected — not measurable; would be fabricated (§4).
3. **`Cost` as total resource cost (energy/hardware) instead of billing.** Rejected as the default —
   not reliably measurable per commit and provider-specific; billing-cost + neutral `Tokens` is the
   honest pair. Left as a possible future extension.
4. **Record `0` for unknown local token counts.** Rejected — conflates "free/zero" with "not measured";
   violates the `-` convention (§2).
5. **Cost-only (no tokens).** Rejected — `Cost` is provider-specific and 0 for local, so it cannot be
   the cross-adapter yardstick; `Tokens` must exist for that.

## Resulting actions

- [x] Specify the trailer schema, value semantics, granularity, and attribution rule (this ADR).
- [ ] **(Gated)** Implement adapter-side capture + `PROVENANCE_TOKENS`/`PROVENANCE_COST` export in
      `adapters/claude-code/` and `adapters/opencode/` when the trigger fires (human `dependency-add`
      gate if any new dependency is required).
- [ ] **(Gated)** Extend `githooks/prepare-commit-msg` to append the new trailers, and `scripts/metrics.sh`
      to aggregate them (skip `-`, total-wins, per-model tokenizer label).
- [ ] Ensure the ai-paced harness maps a session's total to a single commit (§5) to prevent double
      counting.
