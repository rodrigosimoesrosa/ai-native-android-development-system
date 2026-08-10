# Phase 0 Research: App Navigation Shell

Inherited architecture; no new dependencies. A few navigation decisions.

## Shell pattern
**Decision:** a Compose `Scaffold` with a `NavigationBar` (bottom nav) + an **inner** `NavHost` whose
destinations are Home and Profile, using per-destination state preservation
(`saveState`/`restoreState` + `launchSingleTop`). **Rationale:** the Navigation Compose standard for a
multi-tab shell; boring/mainstream (constitution). **Alternatives:** navigation drawer (acceptable
alternative, deferred); a manual pager (rejected — loses nav semantics/back stack).

## Where the shell sits
**Decision:** the shell is the **authenticated area** inside the existing `AuthRoot` (001/US2). When
auth-state resolves to Authenticated, `AuthRoot` shows `NavShell` (whose start destination is Home)
instead of routing straight to the single Home screen. **Rationale:** reuses the existing top-level
auth gating; sign-in stays outside the shell. When the session ends (001/US3), `AuthRoot` already
routes back to sign-in → the shell is dismissed (FR-007).

## Hosting existing screens (no duplication)
**Decision:** Home is the existing `:feature:auth` Home composable; Profile is reached via the
`profile` route id published by `002-user-profile`. The shell **hosts**; it does not reimplement.
**Cross-feature interface:** the `profile` route id string — the single shared point with 002.

## Missing-route resilience
**Decision:** if the Profile route is not present (e.g. 002 not merged yet), the shell renders Home +
a disabled/placeholder Profile tab rather than crashing (FR-006). **Rationale:** graceful degradation
keeps the features independently mergeable (multi-agent).

## Testability
JVM-testable: the destinations list and the auth-gating decision (a small pure/stateful piece).
Nav behavior (switching, state preservation) is validated with **instrumented** Compose nav tests.
