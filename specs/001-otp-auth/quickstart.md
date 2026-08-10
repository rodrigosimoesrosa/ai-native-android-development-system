# Quickstart & Validation: OTP Phone Authentication

Runnable validation that proves the feature end-to-end. Details live in [data-model.md](./data-model.md)
and [contracts/auth-api.md](./contracts/auth-api.md) — this is the run/verify guide, not implementation.

## Prerequisites

- JDK 17, Android SDK (`compileSdk 36`), an emulator/device on `API 26+`.
- Gradle skeleton created with versions pinned per [research.md](./research.md) (first-feature setup).
- Fake `AuthApi` wired for the app; MockWebServer available to tests.

## Build & run the app

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:installDebug         # install on running emulator/device
# launch the app; it opens on SendPhone (no stored session)
```

Manual happy path (Story 1): enter `+15551234567` → tap request → enter the fake code the fake
`AuthApi` accepts → land on **Home** showing the user profile.

## Run the test suite (executable specification)

```bash
./gradlew test                      # pure JVM unit tests (:core, :domain, :feature VMs)
./gradlew :data:testDebugUnitTest   # :data incl. MockWebServer network tests
./gradlew connectedDebugAndroidTest # optional: instrumented DataStore/Compose tests
```

Green across the board = feature done (constitution Principle III).

## Validation scenarios → acceptance criteria / success criteria

| # | Scenario (how to run) | Proves | Layer |
|---|---|---|---|
| 1 | Submit valid phone → advances to VerifyPhone with a challenge | AC1.1, FR-001/002 | `RequestOtpUseCase`, SendPhone VM |
| 2 | Submit correct code → session established → Home | AC1.2, FR-003, SC-001 | `VerifyOtpUseCase`, VerifyPhone VM |
| 2a | Count primary actions from SendPhone to authenticated Home ≤ 3 (submit phone, submit code, +1) — manual/UX check, excludes code-delivery latency | SC-001 | full US1 flow |
| 3 | Home load shows the authenticated user | AC1.3, FR-012 | `GetCurrentUserUseCase`, Home VM |
| 4 | Submit wrong/expired code → clear retryable error | AC1.4, FR-004, SC-006 | VerifyPhone VM error mapping |
| 5 | Invalid phone format → blocked pre-request with message | Edge case, FR-002, SC-006 | `RequestOtpUseCase` guard |
| 6 | Resend too soon → cooldown message (429) | Edge case, FR-005/014 | error → message mapping |
| 7 | Restart app with stored session → Home, no code prompt | AC2.1, FR-006, SC-002 | `ObserveAuthStateUseCase`, DataStore |
| 8 | Access expired + protected call → auto-refresh, action succeeds | AC2.2, FR-008, SC-003 | `TokenAuthenticator` (MockWebServer 401→refresh→retry) |
| 9 | **Concurrent** 401s → exactly one `/auth/refresh` | AC2.3, FR-009, SC-004 | `TokenAuthenticator` single-flight (MockWebServer) |
| 10 | Sign out → session cleared → SendPhone; reopen requires sign-in | AC3.1/3.3, FR-011 | `SignOutUseCase`, routing |
| 11 | Refresh fails (non-renewable) → session ended → SendPhone | AC3.2, FR-010, SC-005 | authenticator returns null → clearSession |
| 12 | App killed mid-verification → restarts at SendPhone | Edge case, FR-013 | challenge is transient (VM state only) |
| 13 | No network on request/verify/protected → recoverable error, no crash | Edge cases, SC-006 | `SafeRemoteDataSource` → `AppError.Network` |
| 14 | Stored session/tokens are never plaintext | FR-007 | encrypted Proto DataStore (Keystore) |

## Key checks to assert

- **Purity**: `:domain`/`:core` compile with no Android/Retrofit/Room import (module deps enforce it).
- **Single-flight**: scenario 9 asserts MockWebServer received exactly one `/auth/refresh` request.
- **Anti-loop**: a `401` on `/auth/refresh` does not recurse; it ends the session (scenario 11).
- **Transient challenge**: no DataStore/Room write occurs for the verification token (scenario 12).

## Automated coverage (as built)

Run `./gradlew test` (all JVM unit tests). Instrumented rows need `connectedAndroidTest` (emulator).

| # | Covered by | Kind |
|---|---|---|
| 1, 5 | `RequestOtpUseCaseTest`, `SendPhoneViewModelTest` | JVM ✅ |
| 2, 4 | `VerifyOtpUseCaseTest`, `AuthApiTest` (200/400/410), `VerifyPhoneViewModelTest` | JVM ✅ |
| 2a | ≤3 primary actions | manual/UX |
| 3 | `HomeViewModelTest` | JVM ✅ |
| 6 | `VerifyPhoneViewModelTest` (resend), `ErrorMessagesTest` (429 → FR-014) | JVM ✅ |
| 7 | `SessionRepositoryTest` (restore), `RootViewModel` routing | JVM ✅ (full flow: instrumented) |
| 8 | `TokenAuthenticatorTest` (401→refresh→retry) | JVM ✅ |
| 9 | `TokenAuthenticatorConcurrencyTest` (exactly one refresh) | JVM ✅ |
| 10 | `SignOutUseCaseTest`, `HomeViewModelTest` (sign-out routes) | JVM ✅ |
| 11 | `TokenAuthenticatorLogoutTest`, `HomeViewModelTest` (auth-state false) | JVM ✅ |
| 12 | `VerifyPhoneViewModelTest` (missing challenge → SendPhone); `PendingVerificationStore` is in-memory | JVM ✅ |
| 13 | `AuthApiTest` (network failure → `AppError.Network`), `ErrorMessagesTest` | JVM ✅ |
| 14 | `CryptoManager` + Proto serializers (Keystore AES/GCM) | instrumented (code-verified) |