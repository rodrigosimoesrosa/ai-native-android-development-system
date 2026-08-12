# ADR-0014: LLM diagnostic telemetry — traces, latency, logs, evals (adapter-scoped)

- **Status:** Proposed (design decided; activation gated — see §Activation)
- **Date:** 2026-08-11
- **Deciders:** Project maintainer
- **Related:** [ADR-0012 (LLM runtime observability — adapter-scoped)](ADR-0012-llm-runtime-observability-adapter-scoped.md) *(this fills its diagnostic half)*, [ADR-0013 (per-commit token/cost aggregate)](ADR-0013-per-commit-token-cost-aggregate.md) *(sibling: the cost metric)*, [ADR-0010 (automated provenance + metrics)](ADR-0010-automated-provenance-and-metrics.md), [ADR-0009 (run modes)](ADR-0009-run-modes-human-paced-and-ai-paced.md), [ADR-0001 (neutral core, adapters)](ADR-0001-build-on-existing-tools-neutral-core.md), [ADR-0007 (quality gates)](ADR-0007-quality-gates-detekt-and-method-guardrails.md), [constitution (Principles III, IV, V)](../docs/constitution.md), [Vision §7 (metrics)](../docs/00-vision-and-architecture.md)

---

## Context

Observability is the ability to **investigate the unexpected** from the signals a system emits — not
just watch pre-chosen alarms. For an LLM/agent system the useful signals fall in four buckets:

- **Metrics** — tokens, cost, latency, error/retry rates (aggregated numbers).
- **Logs** — the actual prompt, context, and response per turn (discrete events with content).
- **Traces** — the per-session span tree: turns → tool calls → retries, with timing at each step.
- **Evals** — a *quality* signal for a non-deterministic output (the hardest, most valuable signal;
  metrics without it are mere accounting).

The project's coverage so far:
- **Process** observability exists and is live (ADR-0010): git-native metrics of the SDD *loop*.
- **Runtime cost** metric is designed (ADR-0013): `Provenance-Tokens`/`Provenance-Cost` per commit.
- The **diagnostic** pillars — **logs, traces, evals** — that answer *"why did this run fail / hang /
  hallucinate / loop?"* are still unspecified. ADR-0012 permits them (adapter-scoped) but did not say
  what they are or what may cross back to the core. This ADR closes that gap.

Two forces (same as ADR-0012/0013):
- **Neutral core / adapters (Principle V):** rich traces/logs are provider-shaped → they live in the
  adapter; only reduced aggregates cross into git.
- **Knowledge in git (Principle IV) + privacy:** prompts/responses contain **repo content and possibly
  secrets** — they must **never** land in git or the neutral core.

## Decision

### 1. Placement (reaffirms ADR-0012)

All diagnostic telemetry — logs, traces, evals — is **adapter-scoped**: it is produced and stored
inside `adapters/<tool>/` against an adapter-chosen sink, and **must not** appear in the neutral core
(`methods/`, `scripts/`, `specs/`, app code, constitution) except as the reduced aggregates in §3.
The adapter boundary (`scripts/check-adapter-boundary.sh`) still holds: **no domain/project knowledge
in the adapter.**

### 2. The three diagnostic pillars, defined

**a. Traces (per session).** A span tree for each agent session: turns, each model call (model +
params, TTFT and total latency), each **tool call** (name, duration, success/failure), **retries**,
and terminal status (ok / error / refusal / cutoff / cancelled). This is the primary *diagnosis*
surface ("where did it go wrong / slow"). Shape: OpenTelemetry-style spans or an LLM-tracing backend
(e.g. Langfuse/Phoenix) — **choice left to each adapter**, not fixed here.

**b. Logs (prompt/response).** Per turn: system prompt, assembled context, user/tool messages, and the
model response. **Sensitive by definition** → governed by §4. Off by default.

**c. Evals (quality).** A quality score for generations. Two modes:
- **Offline evals:** a versioned set of golden prompts/tasks scored by rule or model-judge, run as a
  separate job — the closest LLM analogue to the constitution's "tests as fitness function"
  (Principle III). May live as a neutral, git-versioned *eval set* (prompts + expected properties)
  with execution in an adapter; **scores** (not content) may cross back per §3.
- **Online evals:** per-run signals (heuristics, model-judge, or human thumbs-up/down) attached to a
  session's trace, adapter-side.

### 3. What crosses back into the neutral core (git-native aggregates only)

Only reduced, **content-free** aggregates cross the boundary, via the ADR-0010/0013 trailer mechanism
or `scripts/metrics.sh`. Proposed optional per-commit/session trailer keys (all optional; `-` = not
measured, per ADR-0013 §2):

```
Provenance-Latency-Ms: 42150     # total session wall-time attributed to the commit
Provenance-Retries:    2         # model/tool retries in the session
Provenance-Errors:     0         # failed tool/model calls
Provenance-Outcome:    ok        # ok | error | refusal | cutoff | cancelled
Provenance-Eval:       0.86      # normalized quality score in [0,1], if an eval ran
```

**Never** crosses back: prompts, responses, context, tool arguments/results, or any raw span payload.
Those stay in the adapter sink.

### 4. Privacy & retention (mandatory for logs/traces)

- Prompt/response/context logging is **opt-in**, never a default that ships in the neutral core.
- **Local sink by default**; sending content to an external service is a **human gate** (it publishes
  repo content — treat as irreversible/outward-facing) and an explicit adapter configuration.
- **Secret redaction** before any persistence; a documented **retention/TTL**; content excluded from
  git entirely.
- Contrast with ADR-0013: token **counts**/cost/latency/outcome carry **no content** and are safe in
  git; the §3 aggregates inherit that safety, the §2b logs do not.

### 5. Correlation (trace ↔ commit)

Traces/logs/evals are keyed by a **session id** that maps to the commit via ADR-0013 §5
(session → single commit). So a git-side aggregate (e.g. `Provenance-Outcome: error`) can be pivoted,
during a debugging session, to the full adapter-side trace for that session — without the core ever
storing the trace.

### 6. Sink neutrality

Each adapter picks its own tracing/eval backend; the **git-native aggregate contract (§3) is uniform**,
so cross-adapter comparison (claude-code vs opencode) is possible on the reduced signals even when the
underlying sinks differ. (Local runs: latency/outcome/eval are fully meaningful; cost follows ADR-0013
= `0USD`.)

## The complete observability picture (what this closes)

With this ADR the design is complete across all pillars, each in its correct place:

| Pillar | Where it lives | Crosses to core? | ADR |
|---|---|---|---|
| Process metrics (the SDD loop) | git (native) | is core | ADR-0010 |
| Tokens / cost | adapter → git aggregate | counts only | ADR-0013 |
| Latency / retries / errors / outcome | adapter → git aggregate | reduced only (§3) | **ADR-0014** |
| Traces (spans, tool calls) | adapter sink | no (correlate by id) | **ADR-0014** |
| Logs (prompt/response) | adapter sink, opt-in | **never** | **ADR-0014** |
| Evals (quality) | eval set in git; run in adapter | scores only | **ADR-0014** |

"Complete" = **designed**, not implemented; every pillar now has a decided home, a boundary, and a
privacy stance. Nothing here builds a sink.

## Consequences

### Positive
- The blind spot is closed *by design*: there is now a specified way to answer "why did this run
  fail/hang/hallucinate", not just "what did it cost".
- Evals give the project an LLM-native fitness function aligned with Principle III — quality becomes
  measurable, not asserted.
- Core stays neutral and content-free (Principles IV/V); privacy is explicit for the sensitive pillar.

### Negative / costs
- Real value requires implementing adapter-side tracing + an eval set — non-trivial, gated work.
- Evals are hard to get right (judge bias, flaky scores); a poor eval can mislead. Deliberately left
  unspecified in mechanism here, only placed.
- More trailer keys = more the ai-paced harness must populate; unset ⇒ `-` (no fabrication).

### Neutral
- Backend choice is intentionally per-adapter; only the git-native aggregate is standardized.

## Activation

Dormant until ADR-0012's trigger fires. Suggested phasing when it does: **traces first** (highest
diagnostic value per effort) → latency/outcome aggregates (§3) → an **offline eval set** → opt-in
prompt/response logging last (highest privacy cost). Each step that adds a dependency or an external
sink passes the human `dependency-add` / outward-facing gate.

## Alternatives considered

1. **Put traces/logs in the neutral core / harness.** Rejected — provider-shaped + content-bearing;
   violates Principles IV/V (ADR-0012).
2. **Metrics/cost only, skip evals & traces.** Rejected — that is accounting, not observability; cannot
   diagnose failures or measure quality (the project's own gap analysis).
3. **Log prompts/responses into git.** Rejected hard — secrets/content leakage, repo bloat; git is for
   *knowledge* (Principle IV), not raw session logs.
4. **Merge into ADR-0013.** Rejected — 0013 is content-free/git-safe (counts); this is
   content-bearing/sensitive (logs). Splitting by sensitivity keeps the privacy boundary crisp.
5. **Mandate one backend (e.g. Langfuse) for all adapters.** Rejected — couples adapters to a product;
   only the reduced aggregate needs to be uniform (§6).

## Resulting actions

- [x] Specify the diagnostic pillars, the git-native aggregate additions, privacy/retention, and
      trace↔commit correlation (this ADR); link from `index.md` and CLAUDE.md.
- [ ] **(Gated)** Implement adapter-side tracing in `adapters/claude-code/` and `adapters/opencode/`.
- [ ] **(Gated)** Extend `githooks/prepare-commit-msg` + `scripts/metrics.sh` for the §3 aggregates
      (skip `-`; add `Provenance-Outcome`/`Eval` breakdowns).
- [ ] **(Gated)** Create a versioned **eval set** (neutral prompts + expected properties) and an
      adapter-side runner; surface scores via §3.
- [ ] **(Gated, human outward-facing gate)** Opt-in prompt/response logging with redaction + retention,
      local sink by default.
