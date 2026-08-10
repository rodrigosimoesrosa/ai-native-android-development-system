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
