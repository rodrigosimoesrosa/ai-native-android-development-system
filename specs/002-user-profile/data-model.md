# Phase 1 Data Model: User Profile

Models per layer, mapped at boundaries (ADR-0003). `:domain` is pure.

## Domain entities (`:domain/profile`)

### UserPreferences
| Field | Type | Notes |
|---|---|---|
| `theme` | `Theme` | enum `System | Light | Dark` (default `System`) |
| `notificationsEnabled` | `Boolean` | default `true` |

### User (reused, `:domain/auth`)
Reused from 001. This feature edits `displayName`; `id`/`phone` unchanged.

## Repository interfaces (`:domain/profile`, DIP: domain declares, `:data` implements)

```
IProfileRepository
  suspend fun updateDisplayName(name: String): Result<User>   // PATCH /me, persists the updated User

IPreferencesRepository
  fun observe(): Flow<UserPreferences>
  suspend fun setTheme(theme: Theme): Result<Unit>
  suspend fun setNotifications(enabled: Boolean): Result<Unit>
```

## Models per layer + mapping (`:data/profile`)

| Domain | Remote | Persistence |
|---|---|---|
| `User` (updated) | `UpdateProfileRequest{displayName}`, `MeResponseRemote` (reused) | `UserProto` (reused) |
| `UserPreferences` | — (local only) | `PreferencesProto{theme, notifications_enabled}` — **encrypted** |

Mappers colocated in `:data` (`PreferencesProto ↔ UserPreferences`, `UserRemote.toDomain()` reused).

## Use cases (`:domain/profile/usecase`)

| Use case | Rule | Backs |
|---|---|---|
| `UpdateDisplayNameUseCase` | reject blank → `IProfileRepository.updateDisplayName` | US2 / FR-002,003 |
| `ObservePreferencesUseCase` | `IPreferencesRepository.observe()` | US3 / FR-004 |
| `SetThemeUseCase` / `SetNotificationsUseCase` | persist a preference | US3 / FR-004 |
| *(reuse)* `ObserveUserUseCase` / `GetCurrentUser` from 001 | show the user on Profile | US1 / FR-001 |

## State (ProfileViewModel)

```
load → shows User + Preferences (US1)
edit name (valid) → saving → saved: User updated + reflected (US2)
edit name (blank) → inline error, no save (FR-002)
save fails → recoverable error, no partial persist (FR-006)
toggle preference → persisted immediately; survives restart (US3/FR-004)
```
