# Provenance — `002-user-profile`

Per Constitution Principle IV (*Knowledge in Git, with Provenance*): who/what produced this feature,
recorded outside the app code. Schema: spec, method, agent/model (ADR-0009/0010).

## What

A dedicated **Profile** feature (`:feature:profile`): view the authenticated user, **edit** the
display name (mock `PATCH /me` + local persist), and set **local preferences** (theme, notifications)
in an encrypted Proto DataStore. Reuses `User`/session and the mock `AuthApi` from `001-otp-auth`.

## How (method)

Executed in **`ai-paced`** run mode (ADR-0009): an agent worked the approved `tasks.md` (T001–T026)
to completion via the SDD loop (`methods/sdd-loop.md`) — implement → verify → mark done — gated by
`methods/verify-change.md`. Tests test-first per user story (Constitution Principle III). The four
mandatory human gates (merge, dependency-add, architecture-change, release) were **not** crossed.

| Stage | Artifact |
|---|---|
| Specify/Plan/Tasks | approved `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`, `tasks.md` (human-paced, prior) |
| Implement + Verify | code + tests per phase (Setup → Foundational → US1 → US2 → US3 → Polish), committed per phase |

## Who (agent)

- **Agent:** Claude Code (autonomous executor)
- **Model:** `claude-sonnet-5`
- **Human gates:** merge/dependency-add/architecture-change/release remain with the maintainer. No new
  third-party dependency was added; no new architectural decision was invented.

## Decisions & corrections recorded

- **Preferences default via inversion.** `PreferencesProto.notifications_disabled` is stored inverted
  so the proto3 zero value maps to the domain default `notificationsEnabled = true` (no "unset" flag).
- **Reactive display-name reflection.** Profile observes the session `User` (`ObserveUserUseCase`);
  `ProfileRepository` persists the updated `UserProto` on `PATCH /me` success, so the new name is
  reflected on the next emission without an explicit reload (satisfies AC2.1).
- **Additive session-source method.** `ISessionLocalDataSource.updateUser` added to persist a
  profile edit without touching session tokens (test double updated in lock-step).
- **Per-feature error copy.** `:feature:profile` has its own `AppError.toUserMessage` (profile
  wording, e.g. 400 = invalid name) rather than depending on `:feature:auth` — keeps features
  siblings (ADR-0003 layering), reuses the typed `AppError` contract from `:core`.
- **Test-race fix.** MVI intent→event→reduce is multi-hop async; edit tests advance the scheduler
  between `NameChanged` and `Save` so the draft is applied before save reads it.

## Cross-feature seam

`:feature:profile` publishes the stable `profile` route id + `NavGraphBuilder.profileScreen()`
(`ProfileRoutes.PROFILE`) for `003-navigation` to compose into the app graph. `:app` does **not** yet
depend on `:feature:profile`; wiring Profile into navigation (and the FR-005 unauthenticated route
guard, quickstart scenario 8) is `003`'s integration, resolved at the human merge gate.

## Verification

- All JVM tests green: `:domain:test`, `:data:test`, `:feature:profile:test` (Turbine + MockWebServer
  + unit); `:app:assembleDebug` green (Hilt graph resolves the new profile bindings + preferences
  DataStore). `DomainPurityTest` extended to explicitly cover `:domain/profile`.
- Static analysis (`detekt` + ktlint ruleset, ADR-0007) green across modules.
