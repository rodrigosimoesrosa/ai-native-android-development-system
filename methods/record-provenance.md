# Method: record provenance

**Intent:** every change is traceable to *which specification, which method, and which agent or person
(and which model)* produced it — so "who/what made this, and on whose authority?" always has an
answer. Recorded in **version-control metadata**, never in application code.

## What to record, per change

- **Spec** — the unit of work the change serves.
- **Method** — the capability/stage that produced it (or "manual").
- **Agent** — the agent or person who authored it (or "human").
- **Model** — the model, when an agent authored it.

## How

- Attach the record as **commit trailers** — structured `Key: value` lines in the commit message.
  Metadata, not code: it never appears in the application sources.
- **Automate it**: a commit hook stamps the trailers from the branch/spec and from context provided by
  the driver, so no one has to remember. It must not double-stamp.
- **Make it queryable**: the trailers form a ledger readable straight from history — grouped per spec,
  per agent/model — and the same history yields the process's health metrics.

## Invariants

- Provenance lives in the repository's own history (reproducible by cloning); no external tracker.
- It records authorship and authority; it never gates or blocks — that is the verification method's job.
- The autonomous driver (see [`run-modes`](run-modes.md)) supplies its identity so its changes are
  self-attributing; absent any context, the author defaults to a person.

## Run metrics (tokens + run-health)

Beyond authorship, a change may carry **content-free run metrics** — how much work it took and how
the run went (spec `006`; [ADR-0013](../decisions/ADR-0013-per-commit-token-cost-aggregate.md),
[ADR-0014 §3](../decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md)). Same channel as
provenance (commit trailers), same automation (the hook stamps them).

- **Keys:** `Provenance-Tokens`, `Provenance-Latency-Ms`, `Provenance-Retries`, `Provenance-Errors`,
  `Provenance-Outcome` (`ok` | `error` | `cancelled` — model-level `refusal`/`cutoff` deferred to
  ADR-0014). Cost/USD is deferred to a later phase.
- **`0` vs not-measured:** a measured `0` is recorded as `0`; an unmeasured value is **omitted**, and
  the metrics view skips it. Never fabricate a value; never block a commit.
- **Where the values come from:** a session does not commit, so writers put values in a transient
  sidecar inside `.git/` (`mirabilis/run-metrics.env`, never tracked) — the neutral **harness** writes
  run-health, the tool **adapter** writes tokens ([ADR-0012](../decisions/ADR-0012-llm-runtime-observability-adapter-scoped.md)
  boundary). The hook reads it on the next commit, stamps present keys, and **deletes** it
  (consume-once → one session = one commit, no double counting).
- **Aggregation:** `scripts/metrics.sh` sums them per spec/agent/model; per-model token sums are
  labelled approximate (tokenizers differ across models).
- **Content-free invariant:** only numeric/enum values ever reach git — prompts/responses never do.
