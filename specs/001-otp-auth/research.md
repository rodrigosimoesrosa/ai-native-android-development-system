# Phase 0 Research: OTP Phone Authentication

Architecture, DI, persistence, and networking are **already decided** by ADR-0003 through ADR-0006
and are binding. This document resolves only what those ADRs deferred to "confirm at skeleton
time" (build-tool versions) plus the feature-specific refresh mechanics that must be nailed down
before design.

## Versions (pinned in `gradle/libs.versions.toml`)

**Decision** — pin the following baseline; `libs.versions.toml` is the single source of truth
(constitution: determinism). Versions the ADRs already fixed are carried through unchanged.

| Component | Version | Source |
|---|---|---|
| Kotlin | `2.2.20` | Skeleton choice — enables Compose Compiler Gradle plugin, K2 |
| Gradle (wrapper) | `8.13` | Matched to AGP 8.13.0 |
| Android Gradle Plugin | `8.13.0` | Skeleton choice — keeps the conventional `kotlin.android` setup (AGP 9 removes it) |
| KSP | `2.2.20-2.0.2` | ADR-0004/0005 (KSP, not kapt) |
| compileSdk / targetSdk | `36` | Skeleton choice |
| minSdk | `26` | Android Keystore-backed encryption baseline (ADR-0006 §5) |
| Compose BOM | `2025.09.00` | Skeleton choice |
| Dagger + Hilt | `2.56.2` | ADR-0004 (**use Hilt**); patch corrected at setup — Hilt 2.58+ requires AGP 9, so 2.56.2 is the AGP-8-compatible pin (verified: full graph builds green 2026-08-08) |
| hilt-navigation-compose | `1.2.0` | ADR-0004 (confirm latest at setup) |
| Retrofit | `3.0.0` | **ADR-0006** (fixed) |
| OkHttp | `5.4.0` | **ADR-0006** (fixed) |
| kotlinx.serialization | current stable | ADR-0006 (confirm at setup) |
| Room | `2.8.4` | **ADR-0005** (fixed) — wired at skeleton, unused by this feature |
| DataStore (Proto) | `1.2.1` | **ADR-0005** (fixed, stable not alpha) |
| protobuf runtime + plugin | current stable | ADR-0005 (confirm compatibility at setup) |

**Rationale**: mainstream, boring, deterministic (constitution Principle V). ADR-fixed versions are
non-negotiable; skeleton-choice versions are the newest stable that interoperate cleanly.
**Alternatives considered**: bleeding-edge alphas (DataStore `1.3.0-alpha`) — rejected by ADR-0005
for determinism.

## Test stack

**Decision**: JUnit4 + `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`) + **Turbine**
for Flow assertions + **MockK** (or hand-written fakes for repos/use cases) + **MockWebServer**
(OkHttp) for the network layer. Room `room-testing` in-memory available at skeleton level.
**Rationale**: standard Android/Kotlin test tooling; MockWebServer is required by ADR-0006 to
simulate `401 → refresh → retry` and concurrent 401s. **Alternatives**: Robolectric — avoided for
domain/VM tests since the domain is pure JVM (ADR-0003 §"Why TDD is cheap").

## Transparent refresh — single-flight + anti-loop (ADR-0006 §2–§4)

**Decision**: two OkHttp pieces on the **authed** client.
- `AuthInterceptor` (request `Interceptor`) reads the **in-memory cached** access token from a
  `@Singleton SessionHolder` and adds `Authorization: Bearer <token>`; **skips** `otp/request`,
  `otp/verify`, `auth/refresh` via a no-auth marker (custom header stripped before send, or a
  Retrofit tag).
- `TokenAuthenticator` (OkHttp `Authenticator`) handles `401`: guarded by a `Mutex`
  (single-flight). Before refreshing it compares the failed request's token to the currently
  stored token — if another thread already refreshed, it retries with the current token and does
  **not** refresh again. It refreshes via a **separate Retrofit/OkHttp instance without** the auth
  interceptor/authenticator (anti-loop), persists the new tokens through the session data source
  (DataStore + cache), and returns the original request rebuilt with the new header (OkHttp
  auto-retries). Returns `null` (give up → logout) when there is no refresh token, refresh fails,
  or `response.priorResponse` retry count is exhausted (bail after 1–2).

**Rationale**: `Authenticator` is the idiomatic 401 mechanism; `Mutex` + token re-check gives
exactly-once refresh (SC-004); separate client prevents recursion (SC-005 teardown on failure).
**Alternatives considered**: plain `Interceptor` refresh (rejected — reinvents retry edge cases);
proactive refresh via `expiresIn` (deferred — reactive-on-401 is simpler and robust).

## Reading tokens from a synchronous OkHttp callback

**Decision**: keep an **in-memory cached token** in a `@Singleton SessionHolder`, kept current by
collecting the DataStore `Flow`; interceptor/authenticator read the cache synchronously.
DataStore remains the durable encrypted source of truth; the authenticator writes new tokens
through `ISessionLocalDataSource` (updates both DataStore and cache).
**Rationale**: avoids `runBlocking` on every request (ADR-0006 §4). **Alternatives**:
`runBlocking { dataStore.first() }` per request — rejected (blocks OkHttp threads).

## Token & challenge storage

**Decision**: `SessionProto` (access + refresh + `expiresIn`) and `UserProto` in **Proto DataStore,
encrypted at rest via Android Keystore** (ADR-0006 §5, ADR-0005). The OTP step-1 verification token
is **transient ViewModel state**, never persisted (FR-013).
**Rationale**: single-object typed state → DataStore, not Room (ADR-0005 rule of thumb); encryption
satisfies FR-007. **Alternatives**: Room table for session — rejected (single object, not relational).

## Mock backend

**Decision**: a **fake `AuthApi`** implementation backs the running app; **MockWebServer** backs
network tests. The endpoint contract (see `contracts/auth-api.md`) is the stable seam so a real
backend swaps in by build config later (ADR-0006 §6). Backend owns code format, expiry, resend
cooldown, and attempt/lockout thresholds — the app reflects responses (FR-014), never defines them.
**Rationale**: no public OTP API exists; contract-first keeps behavior stable across the swap.

## Phone validation

**Decision**: validate **E.164-like international format** in the domain (a `RequestOtp` guard /
value check) before calling the backend (FR-002); reject malformed input with a typed error mapped
to a clear message. **Rationale**: spec Assumptions (international/E.164 default); keeps the guard
framework-free in `:domain`. **Alternatives**: `libphonenumber` — deferred as heavyweight for v1;
a format check suffices for the proving ground.

## Open questions

None remaining — all NEEDS CLARIFICATION resolved. Skeleton-choice versions are pinned above and
confirmed exact when the Gradle skeleton is created (a mechanical setup step, not a design unknown).