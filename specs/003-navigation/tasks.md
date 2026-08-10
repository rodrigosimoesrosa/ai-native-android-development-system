# Tasks: App Navigation Shell

**Input**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md),
[quickstart.md](./quickstart.md)

**Tests**: INCLUDED — JVM tests for the stateful slice (destinations, auth-gating); nav behavior is
**instrumented** (`connectedAndroidTest`). Package base `com.mirabilis`.
**Reuse (do NOT rebuild)**: Home (001), Profile route (002), `ObserveAuthStateUseCase` + `AuthRoot` (001).

## Phase 1: Setup

- [x] T001 Create `:feature:navigation` module: add to `settings.gradle.kts`; `feature/navigation/build.gradle.kts` (deps `:feature:auth`, `:feature:profile`, `:core-ui`, Compose, Navigation Compose, Hilt); namespace/manifest. NOTE: `:feature:profile` dep intentionally OMITTED (module absent on this branch — graceful-degradation seam, see T010); Hilt not needed (module has no `@Inject`); manifest auto-generated from namespace.

## Phase 2: Foundational

- [x] T002 [P] `Destination` (route id + label + icon) + the ordered destinations list (`home`, `profile`) in `feature/navigation/src/main/kotlin/com/mirabilis/feature/navigation/`

## Phase 3: US1 — Move between sections (P1) 🎯 MVP

- [x] T003 [P] [US1] JVM test: destinations list is correct; the Authenticated → shell decision holds, in `feature/navigation/src/test/...` (`DestinationsTest.kt`)
- [x] T004 [US1] `NavShell` composable: `Scaffold` + `NavigationBar` + inner `NavHost` hosting **Home** (001) and **Profile** (002 route) in `feature/navigation/.../NavShell.kt`
- [x] T005 [US1] Wire the shell into the existing `AuthRoot`: Authenticated → `NavShell` (instead of routing straight to Home) in `feature/auth/.../navigation/AuthRoot.kt` (+ `:app`). Done via **slot inversion** (`authenticatedContent` slot on `AuthRoot`/`AuthNavGraph`, shell supplied by `:app`/`MainActivity`) to keep the dependency one-way (`navigation → auth`) and avoid a module cycle.

## Phase 4: US2 — Keep each section's state (P2)

- [x] T006 [US2] State preservation on switch (`saveState`/`restoreState`, `launchSingleTop`, per-destination back stack) in `NavShell` (`NavShellScaffold`)
- [x] T007 [P] [US2] Instrumented Compose-nav test: nested state kept across switch in `feature/navigation/src/androidTest/...` (`NavShellStateTest.kt`) — **3 tests PASSED on emulator-5554 (Small_Phone AVD-15)**

## Phase 5: US3 — Authenticated-only, sensible start (P3)

- [x] T008 [P] [US3] JVM test: gating decision (Authenticated → shell(start=home); no session → sign-in) in `feature/navigation/src/test/...` (`NavGatingTest.kt`)
- [x] T009 [US3] Default start destination = `home`; auth gating **reuses** `ObserveAuthStateUseCase` via `AuthRoot` (no session logic re-implemented). `NavGating.entryFor(AuthStartState)` consumes the auth-derived state; `START_DESTINATION = home`.
- [x] T010 [US3] Missing `profile` route → Profile tab disabled/placeholder, Home usable, no crash (FR-006). `PROFILE_ROUTE_AVAILABLE = false` disables the tab; slot shows `ProfileUnavailablePlaceholder`.

## Phase 6: Polish

- [x] T011 [P] Run [quickstart.md](./quickstart.md) JVM scenarios (note: 1–3 need an emulator). JVM (scenarios 4/5 gating, 6 placeholder) green via `:feature:navigation:testDebugUnitTest`; instrumented 1–3 green on emulator-5554.
- [x] T012 [P] Confirm reuse: Home/Profile are hosted (not rebuilt); no auth logic duplicated. Home hosted via `HomeScreen` (001); Profile via shared route id + placeholder; gating reuses `AuthStartState`/`ObserveAuthStateUseCase` — no auth/session logic duplicated.
- [x] T013 Record provenance (spec 003, method, agent/model) per Principle IV — `specs/003-navigation/PROVENANCE.md` + commit trailers (ADR-0010).

## Dependencies & the cross-feature seam

- Setup → Foundational → US1 → US2 → US3 → Polish.
- **T004/T005 consume the `profile` route id from `002-user-profile`** — the single shared point with
  002. If 002 is not merged, T010's graceful degradation keeps `003` independently buildable/mergeable
  (multi-agent disjointness; the overlap is resolved at the human merge gate).

## MVP
Phase 1 + 2 + **US1 (T003–T005)** = a working shell switching Home ↔ Profile.
