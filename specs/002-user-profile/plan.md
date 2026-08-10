# Implementation Plan: User Profile

**Branch**: `002-user-profile` | **Date**: 2026-08-10 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-user-profile/spec.md`

## Summary

A dedicated **Profile** screen for the authenticated user to view and **edit** their display name and
set a small set of **local preferences** (theme, notifications). It **reuses** the session and `User`
from `001-otp-auth` (no auth, no sign-out here) and follows the inherited architecture baseline
(Clean + MVI, pure domain, typed `Result`, Hilt; Proto DataStore for preferences per ADR-0005;
Retrofit for the profile update via the existing mock backend per ADR-0006).

**New module**: `:feature:profile`. New domain use cases + a preferences repository (DataStore) and a
profile-update path (mock `PATCH /me`). The only cross-feature interface is a stable `profile` route.

## Technical Context

**Language/Version**: Kotlin `2.2.20` (inherited). **Primary Dependencies**: Compose, Hilt, Proto
DataStore, Retrofit/OkHttp + kotlinx.serialization (all already pinned). **Storage**: Proto DataStore
(encrypted) for `PreferencesProto`; a display-name change updates `UserProto` + the mock backend.
**Testing**: JUnit + coroutines-test + Turbine (domain/VM); MockWebServer for the update contract.
**Target Platform**: Android `minSdk 26`/`compileSdk 36`. **Project Type**: mobile (multi-module).
**Constraints**: `:domain`/`:core` stay framework-free; only an authenticated user reaches Profile.
**Scale/Scope**: 1 screen, 1 mock endpoint (`PATCH /me`), local preferences.

## Constitution Check

*GATE: pass before Phase 0; re-check after design.*

| # | Principle | Status |
|---|-----------|--------|
| I | Specs are source of truth | PASS — derives from spec.md |
| II | Small units, DIP, SOLID | PASS — new `:feature:profile`; domain declares interfaces, `:data` implements |
| III | Tests as executable spec | PASS — quickstart maps each AC/SC to a test; TDD |
| IV | Knowledge in git + provenance | PASS |
| V | Neutral core, boring tech | PASS — reuses the pinned stack; no new deps |
| — | Reuse over duplication | PASS — reuses `User`/session/sign-out from 001; does not rebuild them |

**Result: PASS.** No new dependencies, no architecture change → Complexity Tracking empty.

## Project Structure

### Documentation (this feature)

```text
specs/002-user-profile/
├── brief.md · spec.md · plan.md · research.md · data-model.md · quickstart.md
├── contracts/profile-api.md
└── tasks.md   (/speckit-tasks)
```

### Source Code

```text
domain/  …/profile/model/UserPreferences.kt (+ Theme enum)
         …/profile/repository/IProfileRepository.kt, IPreferencesRepository.kt
         …/profile/usecase/UpdateDisplayNameUseCase, ObservePreferencesUseCase, SetThemeUseCase, SetNotificationsUseCase
data/    …/profile/preferences/  preferences.proto → PreferencesProto + Serializer (encrypted) + PreferencesLocalDataSource
         …/profile/  ProfileRepository (PATCH /me → update UserProto), PreferencesRepository, mappers, DI binds
         …/auth/network/AuthApi.kt  (+ PATCH /me), FakeAuthApi (+ update)
feature/profile/  ProfileScreen + ProfileViewModel (+ UiState/Intent/Event/Effect); route id "profile"
app/     Hilt: Preferences DataStore instance in DataStoreModule
```

**Structure Decision**: New `:feature:profile` on top of the existing modules (ADR-0003). Preferences
follow the ADR-0005 "single typed object → Proto DataStore" rule. Display-name edit reuses the
`User`/session and the mock `AuthApi`, extended with one endpoint. `:domain`/`:core` unchanged except
the new pure `profile` package.

## Complexity Tracking

> No Constitution Check violations — intentionally empty.
