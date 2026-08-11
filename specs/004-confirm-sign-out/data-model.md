# Data Model: Confirm Sign-Out

**No persisted entities.** This feature adds transient UI state only; session data is owned by spec 001.

## UI state (extends `HomeUiState`)

| Field | Type | Default | Meaning |
|---|---|---|---|
| `showSignOutConfirm` | `Boolean` | `false` | Whether the sign-out confirmation dialog is visible. |
| `signOutError` | `String?` | `null` | Recoverable message when a confirmed sign-out failed (FR-006); `null` otherwise. |

Existing fields (`isLoading`, `user`, `error`) are unchanged.

## Intents (extends `HomeIntent`)

| Intent | Trigger | Effect on state / behavior |
|---|---|---|
| `SignOutRequested` | "Sign out" button tapped | `showSignOutConfirm = true`. **Does not** call `SignOutUseCase`. |
| `SignOutConfirmed` | Confirm action in dialog | Calls `SignOutUseCase`. On success → auth-state observer emits `NavigateToSendPhone`. On failure → `showSignOutConfirm = false`, `signOutError = <message>`, session unchanged. |
| `SignOutCancelled` | Cancel / dismiss dialog | `showSignOutConfirm = false`; no session change; no state lost. |

The prior `HomeIntent.SignOut` (direct sign-out) is removed/replaced by this split.

## State transitions

```text
signed-in (showSignOutConfirm=false)
   │  SignOutRequested
   ▼
confirm-visible (showSignOutConfirm=true)
   ├─ SignOutCancelled ─────────────► signed-in (unchanged)
   └─ SignOutConfirmed ─► SignOutUseCase
            ├─ Success ─► auth-state=false ─► NavigateToSendPhone (→ sign-in)
            └─ Error ───► signed-in, showSignOutConfirm=false, signOutError set
```

## Invariants

- The session ends on **exactly one** path: `SignOutConfirmed` → `SignOutUseCase` success.
- No intent other than `SignOutConfirmed` mutates session state.
- `showSignOutConfirm` is the single source of truth for dialog visibility (survives recreation).
