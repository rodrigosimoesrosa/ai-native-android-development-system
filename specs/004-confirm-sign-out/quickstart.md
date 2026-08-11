# Quickstart: Confirm Sign-Out

Runnable validation for the feature. All behavior is asserted at the ViewModel/state level (JVM) — no
emulator required.

## Prerequisites

- JDK 21, Android SDK (`compileSdk 36`).
- Repo builds green on `main` (specs 001–003 merged).

## Run the tests

```bash
./gradlew :feature:auth:testDebugUnitTest --console=plain   # HomeViewModel scenarios
./gradlew :feature:auth:test detekt --console=plain          # full module tests + static analysis
```

## Scenarios (map to acceptance criteria & the UI contract)

1. **Request opens dialog, no sign-out (C1 / US1-AC1)** — dispatch `SignOutRequested`; assert
   `state.showSignOutConfirm == true` and `SignOutUseCase` was **not** called; user still present.
2. **Confirm signs out (C2 / US1-AC2)** — with the dialog open, dispatch `SignOutConfirmed`; assert
   `SignOutUseCase` was called and the `NavigateToSendPhone` effect was emitted.
3. **Cancel keeps session (C3 / US2-AC1)** — with the dialog open, dispatch `SignOutCancelled`; assert
   `state.showSignOutConfirm == false`, no `SignOutUseCase` call, user unchanged.
4. **Dismiss keeps session (C4 / US2-AC2)** — dismissal maps to `SignOutCancelled`; same assertions as (3).
5. **Failure keeps session (C5 / edge case FR-006)** — configure the sign-out double to return
   `Result.Error`; dispatch `SignOutConfirmed`; assert user still present, `showSignOutConfirm == false`,
   and `signOutError != null`; no `NavigateToSendPhone` effect.
6. **Consequence communicated (C6 / US1-AC3)** — verify the dialog copy states that signing in again
   will be required (asserted in the screen or as a string resource check).

## Manual smoke (optional, needs emulator)

```bash
./gradlew :app:installDebug   # sign in (123456), tap Sign out → dialog → Cancel (stays) / Sign out (exits)
```

## Expected outcome

All JVM scenarios green; a single tap can never end the session (SC-001/SC-004); cancel restores an
intact screen (SC-002).
