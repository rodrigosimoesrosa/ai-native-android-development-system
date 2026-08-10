# Contract: Profile API (extends the mock AuthApi)

Adds one protected endpoint to the existing mock `AuthApi` (ADR-0006 §6). `FakeAuthApi` implements it
for the running app; MockWebServer backs tests. Preferences are **local only** (no endpoint).

## PATCH /me  *(protected)*

Update the authenticated user's editable profile (display name).

- Headers: `Authorization: Bearer <accessToken>` (via the existing `AuthInterceptor`).
- Request: `{ "displayName": "Ada L." }`
- `200 OK`: `{ "user": { "id": "u_1", "phone": "+15551234567", "displayName": "Ada L." } }`
- `400 Bad Request`: `{ "error": "invalid_display_name" }` — blank/invalid (the app also pre-validates, FR-002)
- `401 Unauthorized`: token invalid → the existing `TokenAuthenticator` refresh path applies (001/US2)

On `200`, the repository maps `user → domain User` and persists it (`UserProto`), so Home/Profile show
the new name on next load.

### Error → AppError (reused from `:core`)
`400` → `AppError.Server(400, …)` → non-sensitive message; network failure → `AppError.Network`.
