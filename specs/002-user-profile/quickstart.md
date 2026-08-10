# Quickstart & Validation: User Profile

Runnable validation; details in [data-model.md](./data-model.md) and
[contracts/profile-api.md](./contracts/profile-api.md).

## Prereqs
Built on the 001 skeleton; the app runs against `FakeAuthApi` (now with `PATCH /me`). Sign in first
(reuses 001) to reach an authenticated state.

## Run
```bash
./gradlew test                          # JVM unit tests (:core, :domain, VMs, MockWebServer)
./gradlew :app:assembleDebug            # build
```

## Scenarios → acceptance / success criteria

| # | Scenario | Proves | Layer |
|---|---|---|---|
| 1 | Open Profile → shows display name + phone | AC1.1, FR-001 | `ProfileViewModel`, reuse `ObserveUser` |
| 2 | Profile load fails → clear error + retry | AC1.2, FR-006 | error mapping |
| 3 | Edit name (valid) → save → reflected on reload | AC2.1, FR-002/003, SC-002 | `UpdateDisplayNameUseCase`, `ProfileRepository` |
| 4 | Edit name (blank) → inline error, no save | AC2.2, FR-002, SC-004 | use-case validation |
| 5 | Save fails (network/400) → recoverable error, no partial persist | AC2.3, FR-006 | `PATCH /me` via MockWebServer |
| 6 | Toggle a preference → persisted immediately | AC3.1, FR-004 | `SetThemeUseCase`/`SetNotificationsUseCase` |
| 7 | Change preference → restart → retained | AC3.2, FR-004, SC-003 | `PreferencesLocalDataSource` (Proto DataStore) |
| 8 | Open Profile while unauthenticated → routed to sign-in | Edge, FR-005 | route guard (auth-state) |

## Key checks
- **Purity**: `:domain/profile` has no Android/Retrofit/Room import (guarded by `DomainPurityTest`).
- **Reuse**: no sign-out and no auth logic re-implemented here (owned by 001).
- **Prefs encrypted**: `PreferencesProto` stored via the encrypted DataStore serializer (Keystore).
