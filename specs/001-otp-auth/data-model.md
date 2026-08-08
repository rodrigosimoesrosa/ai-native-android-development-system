# Phase 1 Data Model: OTP Phone Authentication

Models are **per layer** and mapped at every boundary (ADR-0003). The domain is pure; features and
use cases only ever see domain models. Persistence types (`*Proto`) and remote types (`*Remote`)
are confined to `:data`.

## Domain entities (`:domain`, pure Kotlin)

### PhoneVerificationChallenge
The pending request to verify a specific phone. **Strictly transient** — never persisted (FR-013);
lives only as VerifyPhone ViewModel state during the active sign-in flow.

| Field | Type | Notes |
|---|---|---|
| `phone` | `String` | E.164-like, the number being verified |
| `verificationToken` | `String` | Opaque handle from `POST /otp/request` |
| `expiresAt` | `Instant?` | Optional client hint; backend is authoritative on expiry |

Validation: `phone` must pass E.164-like format check (FR-002) before a challenge is requested.

### AuthSession
The signed-in state: short-lived access credential + longer-lived renewal credential. Persisted
securely (FR-006/FR-007), renewable (FR-008), clearable (FR-010/FR-011).

| Field | Type | Notes |
|---|---|---|
| `accessToken` | `String` | Bearer token attached to protected calls |
| `refreshToken` | `String` | Used by `TokenAuthenticator` on `401` |
| `expiresInSeconds` | `Long` | Access-token TTL hint from backend |

Invariant: a valid session has non-blank access + refresh tokens. "No session" is represented as
absent (null) domain session / `authState = false`.

### User
The authenticated person shown on Home (FR-012).

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | Stable identity |
| `phone` | `String` | The verified phone |
| `displayName` | `String?` | Basic profile shown on Home |

## Models per layer + mapping (ADR-0003 / ADR-0005 / ADR-0006)

| Domain | Remote (`:data/network`, `@Serializable`) | Persistence (`:data/datastore`, Proto) |
|---|---|---|
| `AuthSession` | `VerifyResponseRemote` (accessToken, refreshToken, expiresIn, user), `RefreshResponseRemote` | `SessionProto` (accessToken, refreshToken, expiresIn) — **encrypted** |
| `User` | `UserRemote` (id, phone, displayName) | `UserProto` (id, phone, displayName) |
| `PhoneVerificationChallenge` | `OtpRequestResponseRemote` (verificationToken) | **none** — transient, never persisted |

Mappers are extension functions colocated in `:data`, not domain-visible:
`VerifyResponseRemote.toDomain(): AuthSession`, `UserRemote.toDomain(): User`,
`AuthSession.toProto()`, `SessionProto.toDomain()`, `User.toProto()`, `UserProto.toDomain()`.
Read path: `API → *Remote → [map] → domain` (Repository maps). Write path (inverse):
`domain → [map] → *Proto` (SessionLocalDataSource maps). Domain never touches a `*Remote`/`*Proto`.

## Repository interfaces (`:domain/repository`) — DIP: domain declares, `:data` implements

```
IAuthRepository
  suspend fun requestOtp(phone: String): Result<PhoneVerificationChallenge>
  suspend fun verifyOtp(verificationToken: String, code: String): Result<AuthSession>   // also persists session + user
  suspend fun refresh(refreshToken: String): Result<AuthSession>
  suspend fun currentUser(): Result<User>                                                // GET /me (protected)

ISessionRepository
  fun observeAuthState(): Flow<Boolean>            // true while a valid session is stored
  fun observeUser(): Flow<User?>                   // current user for Home
  suspend fun signOut(): Result<Unit>              // clears session + user (FR-011)
  suspend fun clearSession(): Result<Unit>         // teardown on non-renewable session (FR-010)
```

`:data` datasource interfaces (impls extend `Safe*DataSource` → typed `Result`/`AppError`):
`IAuthRemoteDataSource` (Retrofit `AuthApi`), `ISessionLocalDataSource` (DataStore + `SessionHolder`).

## Use cases (`:domain/usecase`, `@Inject constructor`, one rule each)

| Use case | Rule | Backs |
|---|---|---|
| `RequestOtpUseCase` | validate phone (FR-002) → `requestOtp` | Story 1 / FR-001,002 |
| `VerifyOtpUseCase` | `verifyOtp(token, code)` → session established & persisted | Story 1 / FR-003 |
| `ObserveAuthStateUseCase` | expose `observeAuthState()` for routing | Story 1/2/3 / FR-006,012 |
| `GetCurrentUserUseCase` | protected `currentUser()` for Home (exercises refresh) | Story 1/2 / FR-012 |
| `SignOutUseCase` | `signOut()` → clear + route to SendPhone | Story 3 / FR-011 |

Transparent refresh (FR-008/009) is **not** a use case — it is infrastructure in the OkHttp
`TokenAuthenticator` (ADR-0006); the domain never handles 401 or raw tokens. Non-renewable teardown
(FR-010) is triggered by the authenticator returning `null` → session cleared → `authState=false`.

## State machines

### Sign-in flow (navigation)
```
SendPhone --(valid phone submitted, requestOtp ok)--> VerifyPhone(challenge)
SendPhone --(invalid format)--> SendPhone (inline error, FR-002)
VerifyPhone --(correct code, verifyOtp ok)--> Home (session established)
VerifyPhone --(wrong/expired code)--> VerifyPhone (retryable error, FR-004)
VerifyPhone --(resend, cooldown elapsed)--> VerifyPhone (new challenge, FR-005)
VerifyPhone --(rate-limited by backend)--> VerifyPhone (blocking message, FR-014)
* --(app killed mid-flow)--> SendPhone (challenge was transient, FR-013)
```

### Session lifecycle (authState)
```
false --(verifyOtp success)--> true (session persisted)
true  --(access expired, protected call)--> true (authenticator refreshes single-flight, FR-008/009)
true  --(refresh fails / non-renewable)--> false (clearSession, route SendPhone, FR-010)
true  --(signOut)--> false (clear session+user, route SendPhone, FR-011)
```

`observeAuthState()` drives top-level routing: `true` → Home graph, `false` → SendPhone graph.