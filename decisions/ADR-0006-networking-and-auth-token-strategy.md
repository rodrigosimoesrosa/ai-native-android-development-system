# ADR-0006: Networking stack + JWT auth / token-refresh strategy

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [ADR-0003 (architecture)](ADR-0003-android-architecture-clean-mvi.md), [ADR-0004 (DI/Hilt)](ADR-0004-dependency-injection-hilt.md), [ADR-0005 (persistence)](ADR-0005-local-persistence-room-datastore.md), [constitution](../docs/constitution.md)
- **Adapted from:** `plato-app-android` (Retrofit + OkHttp, `SafeRemoteDataSource`, mock API pattern).
- **Drives:** the first feature — OTP authentication with automatic token refresh on 401.

---

## Context

Networking is cross-cutting infrastructure (like persistence) and is decided **once**, before
the first feature. The first feature (two-step OTP auth returning a JWT access token + refresh
token) also needs a **token-refresh strategy**: attach the access token to requests, and on a
`401` transparently refresh and retry. It must fit ADR-0003: networking lives in `:data`,
instances are provided by Hilt in `:app`, and `:domain` never sees Retrofit/OkHttp.

## Decision

### 1. Stack
- **Retrofit `3.0.0`** (typed HTTP) + **OkHttp `5.4.0`** (client, interceptors, authenticator) +
  **kotlinx.serialization** (JSON; Retrofit's kotlinx-serialization converter). Kotlin-first,
  reflection-free, compile-time — the mainstream, boring choice (constitution). Versions pinned
  in `gradle/libs.versions.toml` (kotlinx.serialization: latest stable, confirm at setup).
- Retrofit API interfaces + `XxxRemote` `@Serializable` models live in `:data` (network package).
  `RemoteDataSource` impls extend `SafeRemoteDataSource` → `try/catch` → typed `Result`/`AppError`.
  The `Retrofit`/`OkHttpClient` **instances** are provided by Hilt modules in `:app` (ADR-0004).

### 2. Token attach + refresh — two OkHttp pieces (this is the "interceptor" the feature needs)
1. **`AuthInterceptor` (request `Interceptor`):** reads the current access token and adds
   `Authorization: Bearer <token>`. **Skips** the auth endpoints (`otp/request`, `otp/verify`,
   `auth/refresh`) via a no-auth marker (a custom header stripped before sending, or a Retrofit
   annotation/tag). Public endpoints must not carry a token.
2. **`TokenAuthenticator` (OkHttp `Authenticator`):** the idiomatic 401 handler. On `401`, it uses
   the refresh token to call `auth/refresh`, saves the new tokens, and returns the original request
   rebuilt with the new `Authorization` header (OkHttp auto-retries). Returns `null` (give up) when
   there is no refresh token, the refresh fails, or retries are exhausted.

### 3. Correctness rules (non-negotiable)
- **Single-flight refresh:** guard the refresh with a `Mutex`. Concurrent 401s must trigger **one**
  refresh; waiters then reuse the freshly stored token. Before refreshing, compare the token on the
  failed request against the currently stored token — if another thread already refreshed, just
  retry with the current token (no second refresh).
- **Anti-loop:** the `auth/refresh` call uses a **separate OkHttp/Retrofit instance without** the
  `AuthInterceptor`/`TokenAuthenticator`, so a 401 on refresh can't recurse. Also bound retries via
  `response.priorResponse` count (bail after 1–2 attempts).
- **Refresh failure ⇒ logout:** clear the session, `authState` emits `false`, the app routes to
  `SendPhoneScreen`.

### 4. Reading tokens from a synchronous callback
OkHttp `Interceptor`/`Authenticator` run **synchronously** on OkHttp threads, but DataStore reads
are `suspend`. To avoid `runBlocking` on every request, keep an **in-memory cached token** (a
`@Singleton` session holder updated by collecting the DataStore `Flow`); the interceptor reads the
cache, DataStore remains the durable, encrypted source of truth. The authenticator writes new
tokens through `ISessionLocalDataSource` (which updates both DataStore and the cache).

### 5. Token storage (with ADR-0005)
Access + refresh tokens are the `SessionProto` object in **Proto DataStore, encrypted at rest via
Android Keystore**. `UserProto` (the current user) likewise. The verification token from OTP step 1
is **transient** (ViewModel state), never persisted.

### 6. Mock backend (no public OTP API exists)
A **fake `AuthApi`** backs the running app; **MockWebServer** backs network tests. Contracts are
defined so a real backend can be swapped by build config later.

**Endpoint contract (sketch):**
| Endpoint | Request | Response |
|---|---|---|
| `POST /otp/request` | `{ phone }` | `200 { verificationToken }` |
| `POST /otp/verify` | `{ verificationToken, code }` | `200 { accessToken, refreshToken, expiresIn, user }` |
| `POST /auth/refresh` | `{ refreshToken }` | `200 { accessToken, refreshToken }` |
| `GET /me` *(auth)* | — | `200 { user }` · `401` when token invalid → triggers refresh |

## Consequences

### Positive
- Refresh is transparent to the app; use cases and UI never handle 401 or raw tokens.
- Single-flight + anti-loop + encrypted storage = production-grade, strong portfolio signal.
- `:domain` stays framework-free; networking swappable inside `:data`.

### Negative / costs
- The Authenticator/single-flight/anti-loop logic is subtle — must be covered by tests (MockWebServer
  simulating 401 → refresh → retry, and concurrent 401s).
- In-memory token cache adds a small piece of state to keep in sync with DataStore.
- Retrofit `3.x` / OkHttp `5.x` are recent majors — pin exactly; watch for API deltas vs. older docs.

### Neutral
- Mock backend now; real backend later via config — the contract is the stable part.

## Alternatives considered

1. **Ktor client** instead of Retrofit/OkHttp. Rejected: Retrofit/OkHttp is the maintainer's stack
   and the Android-standard; OkHttp's `Authenticator` gives first-class 401-refresh.
2. **Refresh via a plain `Interceptor`** (not `Authenticator`). Rejected: reinvents retry/So-many
   edge cases the `Authenticator` handles; `Authenticator` is the idiomatic 401 mechanism.
3. **Proactive refresh** (refresh before expiry using `expiresIn`) instead of reactive-on-401.
   Deferred: reactive-on-401 is simpler and robust; proactive can be added later as an optimization.
4. **Moshi/Gson** for JSON. Rejected: kotlinx.serialization is Kotlin-native, reflection-free.

## Resulting actions

- [ ] Pin Retrofit `3.0.0`, OkHttp `5.4.0`, kotlinx.serialization in `libs.versions.toml`.
- [ ] `:data/network`: `AuthApi` (Retrofit) + `@Serializable` `XxxRemote` models + `SafeRemoteDataSource`.
- [ ] `:data`: `AuthInterceptor`, `TokenAuthenticator` (single-flight `Mutex`, anti-loop separate client), in-memory session holder.
- [ ] `:app`: Hilt `NetworkModule` providing both OkHttp clients (authed + refresh-only) and Retrofit.
- [ ] Fake `AuthApi` for the app + MockWebServer tests for the 401→refresh→retry path (incl. concurrent 401s).
