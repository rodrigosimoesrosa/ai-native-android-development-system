# Method: verify a change

**Intent:** define "done" as something a machine checks, and close the loop that drives a change to
that state — the change is complete only when the checks pass.

## The checks (the fitness function)

- **Tests as executable specification** — acceptance criteria expressed as automated tests. Green is
  the default state; a red result is broken work, not progress.
- **Static analysis / formatting** — a single, deterministic style and smell gate over all code.
- **Method guardrails** — the knowledge layer is present and its internal links resolve ("docs are
  tested"), and the neutral core carries no tool-specific coupling.

## The loop

```
change ──► run checks ──► pass ? ──► done
                 ▲          │no
                 └── fix ◄──┘
```

Two feedback surfaces realize it:
- an **in-session** loop that runs the fast checks as work proceeds and returns failures to be fixed;
- a **gate** loop that runs the full set on every proposed change, and is the authority for "done".

## Invariants

- The fast in-session loop must terminate (bounded retries) — feedback, not an infinite spin.
- The gate is the source of truth for merge; the in-session loop is only for speed.
- If a check and the specification disagree, the specification wins and the check (or the spec) is a
  bug to fix — never silently ignored.
