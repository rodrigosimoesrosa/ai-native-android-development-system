# Method: run modes (human-paced / ai-paced)

**Intent:** run the *same* loop in two ways — advanced by a human, or advanced autonomously by an
agent — without forking the system. A run mode is an **orchestration axis**, not a second codebase.

## The one invariant

Both modes execute the same [`sdd-loop`](sdd-loop.md) and must pass the same
[`verify-change`](verify-change.md) gate. The fitness function ("done" = the checks pass) is identical.
Only two things vary:

1. **Driver** — who advances each step of the loop.
2. **Gate policy** — which gates require human judgment (see [`../run-modes.yml`](../run-modes.yml)).

## The two modes

### human-paced
A person advances the loop step by step, in an interactive session, and approves every gate. This is
the default; feedback is immediate and the human sets the pace and scope.

### ai-paced
An agent is the **autonomous executor of an approved plan**. Given an approved breakdown — the task
list produced from an approved specification and plan — it works the list to completion: take the next
unfinished unit → implement it → run the verification gate → on green mark it done → repeat, correcting
failures within a retry bound. When the list is complete and the gate is green it opens a change
request; on repeated failure or no progress it stops and reports. It **never invents scope** — it
executes what was approved, one verifiable unit at a time.

## Handoff boundary (the interface between the two)

The point where a human hands off to the autonomous executor is the **approval of the breakdown** (the
task list). Everything up to and including it — the specification, the plan, and the task breakdown —
is the high-leverage reasoning a human (or a strong model, in human-paced) owns and approves. From that
approval onward, ai-paced executes.

The **approved task list is the contract** between the two: the planner decides *what* and *how*; the
executor implements it against that list, using the gate as the fitness function. This is why a weaker
(e.g. local) executor is viable — each unit is narrow and the gate catches errors. If no approved
breakdown exists, ai-paced has nothing to execute and must not proceed.

## Gate policy (the safety control)

The mandatory human-judgment gates — **merge, architecture change, dependency addition, release** —
remain **human in both modes**. Therefore ai-paced is **not "no humans"**: it is autonomous only over
*small, verifiable units inside an already-approved specification*, and it **escalates** those four
gates to a person. It may produce a complete, verified change; a human still reviews and merges it, and
it may not add a dependency or change the architecture on its own.

## Provenance

Both modes record the same provenance (which specification, which method, which agent/model). In
ai-paced the driver captures it automatically.

## Why one axis, not a fork

Because the loop, the gates, the app, and the knowledge all stay shared, a mode is **configuration + a
driver** — swap the driver, apply the gate policy, and the same verified outcome is produced. Forking
would create two divergent truths; this keeps one.
