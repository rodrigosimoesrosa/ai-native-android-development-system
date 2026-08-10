# Method: record a decision (ADR)

**Intent:** every non-trivial decision leaves a versioned, linkable record, so "why does this exist?"
always has an answer reachable in a few hops.

## When

Record a decision when it is cross-cutting or hard to reverse: an architecture choice, a dependency
addition, a convention, or a correction to an earlier decision. These are also the human-gate points.

## How

Write one file per decision, numbered and immutable once accepted. Capture:

- **Status / date / deciders**, and typed **links** to related decisions (the edges of the knowledge
  graph): *related*, *supersedes/amends*, *closes*.
- **Context** — the forces and the problem.
- **Decision** — what was chosen, concretely.
- **Consequences** — positive, negative/costs, neutral. Be honest about the costs.
- **Alternatives considered** — what else was weighed and why it lost.
- **Resulting actions** — the checklist this decision implies.

## Invariants

- An accepted decision is **not rewritten**; a change is a new decision or an **amendment** appended to
  the existing one (the original text stays as history).
- Superseding, amending, and relating are expressed as **links**, so the decisions form a navigable
  graph whose integrity can be checked (see [`verify-change.md`](verify-change.md)).
- Decisions live beside the code, in version control — reproducible by cloning the repository.
