# Method: the SDD loop

**Intent:** turn a unit of work into a verified change through one repeatable, feedback-driven cycle,
where the specification is the source of truth and tests are the fitness function.

## Stages

1. **Specify** — capture the intent as a machine-readable specification: user stories with priorities,
   acceptance scenarios, functional requirements, measurable success criteria, edge cases. Behavior
   only — no implementation detail. This is the entry point any agent reads first.
2. **Plan** — produce the design: technical context, the architecture *inherited from the recorded
   decisions* (never re-decided per feature), data model, interface contracts, and a runnable
   validation guide. Resolve every open question here.
3. **Break down** — derive a dependency-ordered, executable checklist grouped by user story
   (setup → foundational → per-story → polish), each item with an exact target location. Acceptance
   criteria become tests, written first.
4. **Implement** — execute the checklist bottom-up through the layers, respecting dependencies and
   test-first order, verifying each layer before moving on.
5. **Verify** — see [`verify-change.md`](verify-change.md): run the checks; a change is done only when
   they pass. Failures feed back into implement (and, if the spec and code disagree, back into specify).
6. **Record** — capture provenance and any decision made (see [`write-adr.md`](write-adr.md)), so the
   next cycle starts with complete context.

## Shape

Individually the stages are a pipeline (each consumes the previous artifact). As a *practice* it is a
**feedback control loop**: the spec/tests are the setpoint, verification is the sensor, and the
correction stages (re-specify, reconcile, fix) close the loop. It also loops **per unit of work** — the
whole cycle repeats for the next specification.

## Invariants

- No implementation starts without an approved specification.
- Prefer many small, reviewable changes over few large ones; each maps to a scoped spec.
- Human judgment is mandatory only at defined gates (merge, architecture change, dependency add,
  release); everything else is automated.
