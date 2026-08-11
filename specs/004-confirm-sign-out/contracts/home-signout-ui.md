# UI Interaction Contract: Home Sign-Out Confirmation

The cross-layer contract for this feature is a **UI interaction contract** (no network/API surface).
It defines what the Home surface guarantees around sign-out.

## Surface

`HomeScreen` (module `:feature:auth`, hosted by `NavShell` in `:feature:navigation`), shown to an
authenticated user.

## Contract

| # | Given | When | Then |
|---|---|---|---|
| C1 | Authenticated Home is shown | user taps **Sign out** | a confirmation dialog appears; session is unchanged |
| C2 | Confirmation dialog is shown | user taps **Sign out** (confirm) | `SignOutUseCase` runs; on success the app routes to the sign-in screen |
| C3 | Confirmation dialog is shown | user taps **Cancel** | dialog closes; user stays signed in on Home |
| C4 | Confirmation dialog is shown | user dismisses (tap-outside / back) | dialog closes; user stays signed in; no state lost |
| C5 | Confirmation dialog is shown | user confirms and `SignOutUseCase` fails | dialog closes; user stays signed in; a recoverable message is shown |
| C6 | Confirmation dialog states the consequence | user reads it | it communicates that they will need to sign in again |

## Non-goals

- No change to *where* Sign out lives, to session/token handling (owned by 001), or to navigation
  structure (owned by 003).
- No new persisted data, endpoint, or third-party dependency.

## Verification

Contract rows map to `HomeViewModelTest` cases (state-level) — see [quickstart.md](../quickstart.md).
C1/C3/C4 assert `showSignOutConfirm` and that `SignOutUseCase` was **not** invoked; C2 asserts it was
invoked and the `NavigateToSendPhone` effect emitted; C5 asserts session preserved + `signOutError` set.
