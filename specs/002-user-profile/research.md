# Phase 0 Research: User Profile

Architecture is inherited (ADR-0003–0006); no new dependencies. Only a few small decisions.

## Preferences storage
**Decision:** a `PreferencesProto` in **Proto DataStore, encrypted** (reuse the ADR-0005 pattern +
the existing `CryptoManager`). **Rationale:** preferences are a small single typed object → DataStore,
not Room. **Alternatives:** Preferences DataStore (rejected — untyped, ADR-0005 prefers Proto).

## Display-name update
**Decision:** a mock `PATCH /me { displayName } → 200 { user }` on the existing `AuthApi`/`FakeAuthApi`;
the repository updates the persisted `UserProto` on success. **Rationale:** reuses the ADR-0006 network
seam + the single-user DataStore. **Alternatives:** local-only edit (rejected — the app should reflect a
backend update, matching 001's contract-first stance; the fake still stands in for the backend).

## Theme application
**Decision:** the theme preference is a domain value (`Theme = System | Light | Dark`); applying it to
the UI (MaterialTheme) is a presentation concern in `:app`/`:feature`. **Rationale:** keeps `:domain`
framework-free. **Out of scope for v1:** dynamic color / per-screen theming.

## Reuse from 001 (no duplication)
`User`, the session (`ISessionRepository`/`observeUser`), and **sign-out** are reused as-is. Profile
neither authenticates nor signs out. The Home screen already shows basic user info; Profile adds the
**edit** + **preferences** that Home does not.

## Open questions — resolved
- Preferences in v1: **theme + notifications** (spec assumption). More later.
- Edit path: **mock `PATCH /me`** + persist locally (above).
