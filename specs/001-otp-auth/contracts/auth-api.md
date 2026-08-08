# Contract: Auth API (mock backend)

The app's external interface for this feature. A **fake `AuthApi`** implements it in the running app;
**MockWebServer** serves it in tests. This contract is the stable seam — a real backend swaps in by
build config without changing user-facing behavior (ADR-0006 §6). JSON via kotlinx.serialization.

**The backend owns** code format, code expiry, resend cooldown, and attempt/lockout thresholds. The
app reflects the backend's responses (FR-014) and never defines these limits.

Base: `POST`/`GET` JSON over HTTPS. Auth endpoints (`otp/request`, `otp/verify`, `auth/refresh`)
carry **no** `Authorization` header (public); `GET /me` is protected.

## POST /otp/request

Request a one-time code for a phone number.

- Request: `{ "phone": "+15551234567" }`
- `200 OK`: `{ "verificationToken": "<opaque>" }`
- `400 Bad Request`: `{ "error": "invalid_phone" }` — malformed phone (app also pre-validates, FR-002)
- `429 Too Many Requests`: `{ "error": "rate_limited", "retryAfterSeconds": <int> }` — resend
  cooldown / lockout (FR-005/FR-014)

## POST /otp/verify

Verify the received code for a pending request; on success establish a session.

- Request: `{ "verificationToken": "<opaque>", "code": "123456" }`
- `200 OK`:
  ```json
  {
    "accessToken": "<jwt>",
    "refreshToken": "<jwt>",
    "expiresIn": 900,
    "user": { "id": "u_1", "phone": "+15551234567", "displayName": "Ada" }
  }
  ```
- `400 Bad Request`: `{ "error": "invalid_code" }` — wrong code (retryable, FR-004)
- `410 Gone`: `{ "error": "code_expired" }` — expired code; user requests a new one (FR-004/FR-005)
- `429 Too Many Requests`: `{ "error": "too_many_attempts", "retryAfterSeconds": <int> }` —
  attempt lockout enforced by backend (FR-014)

## POST /auth/refresh

Exchange a refresh token for a fresh session. Called **only** by `TokenAuthenticator` on a `401`,
via the anti-loop client (no auth interceptor).

- Request: `{ "refreshToken": "<jwt>" }`
- `200 OK`: `{ "accessToken": "<jwt>", "refreshToken": "<jwt>" }`
- `401 Unauthorized`: refresh token invalid/expired → authenticator gives up → session cleared,
  `authState=false`, route to SendPhone (FR-010)

## GET /me  *(protected)*

Return the authenticated user's profile. Exercises transparent refresh.

- Headers: `Authorization: Bearer <accessToken>`
- `200 OK`: `{ "user": { "id": "u_1", "phone": "+15551234567", "displayName": "Ada" } }`
- `401 Unauthorized`: access token invalid/expired → OkHttp invokes `TokenAuthenticator`
  (refresh → retry). If refresh also fails → logout path (FR-010).

## Error → AppError mapping (`:data`)

| HTTP | AppError | UI outcome |
|---|---|---|
| network failure / no connectivity | `AppError.Network` | recoverable error, no crash (FR-002/edge cases) |
| `4xx`/`5xx` | `AppError.Server(status, body)` | mapped to clear, non-sensitive message (FR-004/FR-014) |
| unexpected | `AppError.Unknown(throwable)` | generic recoverable error |

`Safe*DataSource` converts any throwable into a typed `AppError`; no raw exception crosses a
boundary (ADR-0003 communication contract).

## Concurrency contract (tested via MockWebServer)

- Several protected calls hitting `401` at once trigger **exactly one** `/auth/refresh`
  (single-flight `Mutex`); waiters reuse the freshly stored token (FR-009 / SC-004).
- `/auth/refresh` never recurses on its own `401` (separate anti-loop client; bounded retries).