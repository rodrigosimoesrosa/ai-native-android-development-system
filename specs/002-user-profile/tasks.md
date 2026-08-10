# Tasks: User Profile

**Input**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md),
[contracts/profile-api.md](./contracts/profile-api.md), [quickstart.md](./quickstart.md)

**Tests**: INCLUDED, tests-first (Constitution Principle III). Package base `com.mirabilis`.
**Reuse (do NOT rebuild)**: `User`/session/sign-out from 001, `CryptoManager`, `ErrorMessages`, the MVI base.

## Phase 1: Setup

- [x] T001 Create `:feature:profile` module: add to `settings.gradle.kts`; `feature/profile/build.gradle.kts` (deps `:domain`,`:core`,`:core-ui`, Compose, Hilt, hilt-navigation-compose); namespace/manifest.

## Phase 2: Foundational (blocking)

- [x] T002 [P] `:domain` `UserPreferences` + `Theme` enum in `domain/src/main/kotlin/com/mirabilis/domain/profile/model/`
- [x] T003 [P] `:domain` `IProfileRepository` + `IPreferencesRepository` in `domain/src/main/kotlin/com/mirabilis/domain/profile/repository/`
- [x] T004 [P] `:data` `preferences.proto` in `data/src/main/proto/preferences.proto`
- [x] T005 `:data` `PreferencesProto` `Serializer` (encrypted, reuse `CryptoManager`) + `PreferencesLocalDataSource` in `data/.../profile/preferences/` — depends T004
- [x] T006 `:data` add `PATCH /me` to `AuthApi` + `UpdateProfileRequest`/reuse `MeResponseRemote`; implement in `FakeAuthApi` in `data/.../auth/network/`
- [x] T007 `:data` mappers `PreferencesProto ↔ UserPreferences` in `data/.../profile/mapper/`
- [x] T008 `:data` `ProfileRepository` (updateDisplayName → PATCH /me → persist `UserProto`) + `PreferencesRepository` impls + `@Binds` in `data/.../profile/` — depends T003,T005,T006,T007
- [x] T009 `:app` provide the Preferences `DataStore<PreferencesProto>` in `DataStoreModule` — depends T005

## Phase 3: US1 — View my profile (P1) 🎯 MVP

- [x] T010 [P] [US1] ViewModel test (Turbine): Profile loads the authenticated user in `feature/profile/src/test/...`
- [x] T011 [US1] `ProfileViewModel` + `UiState/Intent/Event/Effect` (reuse `ObserveUser`/`GetCurrentUser`) in `feature/profile/.../profile/`
- [x] T012 [US1] `ProfileScreen` Compose (show display name + phone; error + retry — FR-001/006) in `feature/profile/.../profile/`
- [x] T013 [US1] Expose route id `profile` + register `ProfileScreen` in `feature/profile/.../navigation/` (the cross-feature interface for 003)

## Phase 4: US2 — Edit my display name (P2)

- [ ] T014 [P] [US2] Unit test `UpdateDisplayNameUseCase` (reject blank) in `domain/src/test/...`
- [ ] T015 [P] [US2] MockWebServer test `PATCH /me` (200 → updated user; 400 → typed error) in `data/src/test/...`
- [ ] T016 [US2] `UpdateDisplayNameUseCase` (validate non-blank → repo) in `domain/.../profile/usecase/`
- [ ] T017 [US2] Edit intent in `ProfileViewModel` (saving/saved/error states) — depends T011, T016, T008
- [ ] T018 [US2] Edit UI in `ProfileScreen` (name field + Save + inline error — FR-002/006)

## Phase 5: US3 — Set my preferences (P3)

- [ ] T019 [P] [US3] Tests: `ObservePreferences`/`SetTheme`/`SetNotifications` + persistence across reload in `domain`/`data` tests
- [ ] T020 [P] [US3] `ObservePreferencesUseCase`, `SetThemeUseCase`, `SetNotificationsUseCase` in `domain/.../profile/usecase/`
- [ ] T021 [US3] Preferences intents in `ProfileViewModel` + toggles (theme, notifications) in `ProfileScreen` — depends T020, T008
- [ ] T022 [US3] Apply the `theme` preference to `MaterialTheme` in `:app` (or a theme holder) — depends T020

## Phase 6: Polish

- [ ] T023 [P] Run [quickstart.md](./quickstart.md) scenarios 1–8
- [ ] T024 [P] Error-message mapping for profile errors (reuse `feature/auth` `ErrorMessages` or a shared helper)
- [ ] T025 [P] Purity: `:domain/profile` has no framework import (extend `DomainPurityTest` coverage)
- [ ] T026 Record provenance (spec 002, method, agent/model) per Principle IV

## Dependencies

- Setup → Foundational (T002–T009) → US1 (T010–T013) → US2 (T014–T018) → US3 (T019–T022) → Polish.
- **Cross-feature interface:** T013 publishes the `profile` route id that `003-navigation` consumes —
  the single shared point, resolved at the human merge gate (multi-agent disjointness).

## MVP
Phase 1 + 2 + **US1 (T010–T013)** = view your profile. Deploy/demo, then add edit (US2) and preferences (US3).
