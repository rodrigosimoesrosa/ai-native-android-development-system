# Research: Confirm Sign-Out

No open `NEEDS CLARIFICATION` — the feature reuses established mechanisms. Decisions below record the
approach and the alternatives rejected.

## Decision 1 — Confirmation as transient ViewModel state (not navigation)

- **Decision**: Model the dialog as a boolean `showSignOutConfirm` on `HomeUiState`, toggled by
  intents (`SignOutRequested` → true, `SignOutCancelled`/confirm → false). The Compose `AlertDialog`
  renders when the flag is true.
- **Rationale**: Matches the existing MVI pattern (ADR-0003); state survives configuration change for
  free (dialog stays open on rotation, per spec edge case); fully unit-testable via Turbine without an
  instrumented test.
- **Alternatives rejected**: A separate navigation destination/route for the dialog (overkill,
  introduces back-stack complexity for a modal); holding the flag in Compose `remember` only (lost on
  rotation, not ViewModel-testable).

## Decision 2 — Reuse `SignOutUseCase`, gate it behind confirm

- **Decision**: Keep the single `SignOutUseCase` (spec 001) as the only sign-out path. `SignOutConfirmed`
  calls it; nothing else does. The old direct `HomeIntent.SignOut → signOutUseCase()` wiring is replaced
  by the request/confirm split.
- **Rationale**: No new sign-out logic (FR-003); the auth-state observer still drives the route-out, so
  the confirm path reuses the existing `NavigateToSendPhone` effect unchanged.
- **Alternatives rejected**: A new confirm-specific use case (duplicates 001, violates reuse).

## Decision 3 — Handle sign-out failure explicitly (FR-006)

- **Decision**: `SignOutConfirmed` inspects the `Result` of `SignOutUseCase`. On `Error`, keep the user
  signed in, close the dialog, and set a recoverable `signOutError` message (reusing `toUserMessage()`).
  On `Success`, the existing auth-state observer emits `NavigateToSendPhone`.
- **Rationale**: Today the result is ignored; the spec now requires no indeterminate half-signed-out
  state. Surfacing a recoverable message matches the existing error-handling convention.
- **Alternatives rejected**: Ignoring the result (current behavior; violates FR-006); rety loops
  (out of scope).

## Decision 4 — Testing strategy: JVM (Turbine), no new instrumented test

- **Decision**: Extend `HomeViewModelTest` with the request/confirm/cancel/failure scenarios. The dialog
  itself is a thin declarative surface bound to state.
- **Rationale**: The behavior worth protecting is the state machine (request opens dialog without
  signing out; confirm signs out; cancel preserves session; failure preserves session + error). That is
  all JVM-assertable. Avoids emulator dependency for a modal.
- **Alternatives rejected**: A new instrumented Compose test (adds emulator cost for behavior already
  covered at the state level).
