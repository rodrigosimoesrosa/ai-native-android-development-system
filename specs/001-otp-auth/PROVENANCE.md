# Provenance — `001-otp-auth`

Per Constitution Principle IV (*Knowledge in Git, with Provenance*): who/what produced this feature,
recorded outside the app code.

## What

OTP phone authentication with session continuity — the **first feature** of the system, which also
stood up the multi-module skeleton (`:core`, `:core-ui`, `:domain`, `:data`, `:feature:auth`, `:app`).

## How (method)

Driven end-to-end by the **GitHub Spec Kit** workflow (ADR-0002), one skill per stage:

| Stage | Skill | Output |
|---|---|---|
| Specify | `/speckit-specify` | `spec.md` |
| Plan | `/speckit-plan` | `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md` |
| Tasks | `/speckit-tasks` | `tasks.md` (63 tasks) |
| Analyze | `/speckit-analyze` | cross-artifact consistency (remediations G1/G2/N1/I1 applied) |
| Implement | `/speckit-implement` | code + tests, per user story |

## Who (agent)

- **Agent:** Claude Code
- **Model:** `claude-sonnet-5`
- **Human gates:** the maintainer approved the plan, committed at checkpoints, and owns the merge gate.
  Human/agent split: the agent authored the specs, plan, tasks, code, and tests; the human directed
  scope (per-user-story stops), committed, and approves dependency/architecture decisions.

## Decisions & corrections recorded

- **[ADR-0004 Amendment 1]** Hilt pinned `2.56.2` (not `2.60.1`); AGP-8 baseline kept (AGP 9 removes
  the `kotlin.android` plugin). Verified green. Awaiting merge-gate approval as a dependency change.
- **MVI base fix** (`:core-ui`): one-shot effects moved from a replay-0 `SharedFlow` to a buffered
  `Channel`, so an effect emitted before the UI subscribes (e.g. an `init`-time redirect) is delivered
  exactly once. Surfaced by a failing test during US1.

## Verification

- Local `Stop` hook (`scripts/gradle-verify.sh`) ran the fast JVM tests each turn during development.
- Full suite + `:app:assembleDebug` green after each user story (US1, US2, US3) and Polish.
- The transparent-refresh mechanism (single-flight, anti-loop, non-renewable teardown) is validated by
  MockWebServer tests, not the running fake app (ADR-0006 §6).
