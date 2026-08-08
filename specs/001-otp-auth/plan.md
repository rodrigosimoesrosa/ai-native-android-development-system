# Implementation Plan: OTP Phone Authentication with Session Continuity

**Branch**: `001-otp-auth` | **Date**: 2026-08-07 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-otp-auth/spec.md`

## Summary

Passwordless sign-in: a user submits a phone number, receives an out-of-band one-time code,
verifies it, and lands on an authenticated Home showing their profile. The session (JWT access +
refresh tokens) is persisted encrypted at rest; the access token is auto-refreshed on `401`
transparently, single-flight, with anti-loop guarantees. Sign-out and non-renewable sessions
return the user to sign-in with credentials cleared.

**Technical approach** (inherited, not rediscovered — see constitution and ADRs 0003–0006):
pragmatic **Clean Architecture + MVI** across Gradle modules (`:core`, `:core-ui`, `:domain`,
`:data`, `:feature:auth`, `:app`) with a **pure domain**, **Hilt** DI, **Retrofit 3 / OkHttp 5**
networking, **Proto DataStore** (Keystore-encrypted) for the session + user, and typed
`Result`/`AppError` at every boundary. Transparent refresh is an OkHttp `AuthInterceptor`
(attach bearer) + `TokenAuthenticator` (single-flight 401 refresh on a separate anti-loop client).
A **fake `AuthApi`** backs the running app; **MockWebServer** backs network tests.

**This is the first feature**, so it also stands up the module skeleton, the `:core` contracts,
and the `:core-ui` MVI base (ported from `mvi-sample/` with the two ADR-0003 fixes).

## Technical Context

**Language/Version**: Kotlin `2.2.20` (JVM target 17); pure JVM for `:core`/`:domain`.

**Primary Dependencies**: Jetpack Compose (BOM), Hilt/Dagger `2.56.2` (KSP; AGP-8 compatible), Retrofit `3.0.0` +
OkHttp `5.4.0` + kotlinx.serialization, Room `2.8.4`, Proto DataStore `1.2.1` + protobuf,
Navigation Compose, `hilt-navigation-compose`.

**Storage**: Proto DataStore (encrypted via Android Keystore) for `SessionProto` + `UserProto`.
No Room table is required by this feature (session/user are single objects, per the ADR-0005
"single object → DataStore" rule). Room's Gradle dependency is available in `:data` per the
architecture baseline but is **not exercised by this feature** — no Room artifact is created here;
the first relational feature will add `XxxEntity`/DAO.

**Terminology mapping (spec → implementation)**: the spec's implementation-agnostic "short-lived
access credential" / "longer-lived renewal credential" concretize here to a JWT **access token** /
**refresh token** (ADR-0006); "authenticated session" → `AuthSession`.

**Testing**: JUnit + `kotlinx-coroutines-test` + Turbine (pure domain/VM), MockK/fakes,
MockWebServer (401→refresh→retry incl. concurrent 401s), Room in-memory only where relevant.

**Target Platform**: Android — `minSdk 26` (Keystore-backed encryption), `compileSdk`/`targetSdk 36`.

**Project Type**: Mobile app (single Android app, multi-module).

**Performance Goals**: UI at 60 fps; transparent refresh completes an in-flight protected call
with no visible re-auth in ≥ 99% of cases (SC-003); exactly one refresh under concurrent 401s
(SC-004).

**Constraints**: `:domain` and `:core` are framework-free (no Android/Retrofit/Room imports);
tokens never readable in plaintext; the pending verification challenge is transient (never
persisted); network required to sign in.

**Scale/Scope**: 3 screens (SendPhone, VerifyPhone, Home), 4 mock endpoints, one feature module,
single device / single session (multi-device out of scope for v1).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| # | Principle | Gate | Status |
|---|-----------|------|--------|
| I | Specs are source of truth | Work traces to `spec.md`; code/spec disagreements are bugs | PASS — plan derives from spec + ADRs |
| II | Small units, explicit boundaries, SOLID/DIP | Domain defines repo interfaces; `:data` implements; arrows point inward; module-per-layer enforced by compiler | PASS — module map per ADR-0003 |
| III | Tests as executable specification | Acceptance scenarios become tests; TDD; green CI = done | PASS — quickstart maps each AC/SC to a test |
| IV | Knowledge in git with provenance | Spec, ADRs, plan, contracts all versioned & cross-linked | PASS |
| V | Neutral core, pluggable adapters; boring tech | No domain logic in tool adapters; mainstream stack | PASS — Retrofit/Room/Hilt/Compose |
| — | Android is proving ground, not showcase | Feature exercises the system end-to-end, non-trivial | PASS — full read (`/me`) + write (verify) + refresh path |
| — | Determinism/reproducibility | Versions pinned in `libs.versions.toml`; scripted | PASS — see research.md §Versions |

**Result: PASS. No violations → Complexity Tracking left empty.** The multi-module split and two
storage mechanisms are mandated by the constitution/ADRs, not discretionary complexity.

## Project Structure

### Documentation (this feature)

```text
specs/001-otp-auth/
├── plan.md              # This file
├── research.md          # Phase 0 output — versions + refresh/single-flight decisions
├── data-model.md        # Phase 1 output — entities, models-per-layer, mappers, state machines
├── quickstart.md        # Phase 1 output — runnable validation mapped to ACs/SCs
├── contracts/
│   └── auth-api.md       # Phase 1 output — mock AuthApi endpoint contracts
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

First feature stands up the skeleton; auth code lands in `:feature:auth`, `:domain`, `:data`.

```text
settings.gradle.kts · build.gradle.kts · gradle/libs.versions.toml   # skeleton + pinned versions

core/                         # :core — pure Kotlin/JVM (no Android)
└── src/main/kotlin/…/core/
    ├── result/               # Result<T>, FlowResult<T>, AppError, fold/asResult helpers
    └── dispatcher/           # @Dispatcher qualifier + Dispatchers abstraction

core-ui/                      # :core-ui — Android lib, MVI base (ported from mvi-sample/)
└── src/main/kotlin/…/core/ui/mvi/   # MVIViewModel, Reducer, UiState/Event/Effect/Intent (fixes applied)

domain/                       # :domain — pure Kotlin/JVM → depends :core
└── src/main/kotlin/…/domain/auth/
    ├── model/                # PhoneVerificationChallenge, AuthSession, User
    ├── repository/           # IAuthRepository, ISessionRepository (INTERFACES only)
    └── usecase/              # RequestOtp, VerifyOtp, RefreshSession, SignOut, ObserveAuthState, GetCurrentUser

data/                         # :data — Android lib → depends :domain, :core
└── src/main/kotlin/…/data/auth/
    ├── network/              # AuthApi (Retrofit), *Remote @Serializable models, SafeRemoteDataSource, FakeAuthApi
    ├── network/interceptor/  # AuthInterceptor, TokenAuthenticator (single-flight Mutex, anti-loop)
    ├── datastore/            # session.proto/user.proto → SessionProto/UserProto, Serializers, SessionLocalDataSource
    ├── session/              # SessionHolder (in-memory cached token @Singleton)
    ├── repository/           # AuthRepository, SessionRepository (impls, mapping to domain)
    ├── mapper/               # *Remote.toDomain(), Session.toProto() etc. (data-visible only)
    └── di/                   # RepositoriesModule, DataSourcesModule (@Binds)

feature/auth/                 # :feature:auth — Android lib → depends :domain, :core, :core-ui
└── src/main/kotlin/…/feature/auth/
    ├── sendphone/            # SendPhoneScreen + SendPhoneViewModel (@HiltViewModel) + UiState/Intent/Event/Effect
    ├── verifyphone/          # VerifyPhoneScreen + VerifyPhoneViewModel + …
    ├── home/                 # HomeScreen + HomeViewModel + …
    └── navigation/           # AuthNavGraph (SendPhone → VerifyPhone → Home), auth-state routing

app/                          # :app — Android app, composition root
└── src/main/kotlin/…/app/
    ├── MirabilisApp.kt        # @HiltAndroidApp
    ├── MainActivity.kt        # NavHost host
    └── di/                    # NetworkModule (authed + refresh-only clients), DataStoreModule, DispatchersModule
```

**Structure Decision**: Multi-module Clean Architecture exactly as ADR-0003 mandates
(`:data → :domain → :core`; `:feature:auth` and `:app` on top). The compiler enforces the
Dependency Rule. Auth is the first `:feature:*`. Session/user persist via Proto DataStore (ADR-0005);
Room is wired but unused by this feature (single-object data → DataStore, per the ADR-0005 rule).

## Complexity Tracking

> No Constitution Check violations — section intentionally empty.