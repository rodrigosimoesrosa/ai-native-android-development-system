# Tasks: OTP Phone Authentication with Session Continuity

**Input**: Design documents from `/specs/001-otp-auth/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/auth-api.md](./contracts/auth-api.md),
[quickstart.md](./quickstart.md)

**Tests**: INCLUDED and tests-first. The constitution makes *Tests as Executable Specification*
NON-NEGOTIABLE (Principle III), so every story writes failing tests before implementation.

**Organization**: Tasks are grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: US1 / US2 / US3 (Setup, Foundational, Polish carry no story label)
- Package base `com.mirabilis`; module dirs per [plan.md](./plan.md): `core/`, `core-ui/`,
  `domain/`, `data/`, `feature/auth/`, `app/`.

---

## Phase 1: Setup (Shared Infrastructure — first-feature skeleton)

**Purpose**: Stand up the multi-module Gradle project per ADR-0003.

- [ ] T001 Create Gradle skeleton at repo root: `settings.gradle.kts` (include `:core`, `:core-ui`, `:domain`, `:data`, `:feature:auth`, `:app`), root `build.gradle.kts`, `gradle.properties`, Gradle wrapper
- [ ] T002 [P] Create `gradle/libs.versions.toml` pinning every version from [research.md](./research.md) (Kotlin 2.2.20, AGP 8.13.0, KSP, Hilt 2.60.1, Retrofit 3.0.0, OkHttp 5.4.0, Room 2.8.4, DataStore 1.2.1, protobuf, Compose BOM, kotlinx.serialization)
- [ ] T003 Create per-module `build.gradle.kts` for all 6 modules with the ADR-0003 dependency wiring (`:data → :domain → :core`; `:core-ui` Android lib; `:feature:auth → :domain,:core,:core-ui`; `:app → all`) and plugins (KSP, compose, kotlinx.serialization, protobuf, hilt) — depends on T001, T002
- [ ] T004 [P] Configure ktlint/spotless + `.editorconfig` at repo root
- [ ] T005 [P] Add Android manifests + namespaces for `:app` and library modules (`minSdk 26`, `compileSdk`/`targetSdk 36`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core contracts, MVI base, domain interfaces, and `:data`/`:app` infrastructure shared
by ALL stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T006 [P] `:core` — `Result<T>`, `FlowResult<T>`, `AppError` sealed types + `fold`/`asResult` helpers in `core/src/main/kotlin/com/mirabilis/core/result/`
- [ ] T007 [P] `:core` — dispatchers abstraction + `@Dispatcher(IO|Default)` qualifier in `core/src/main/kotlin/com/mirabilis/core/dispatcher/`
- [ ] T008 [P] `:core` — unit tests for `Result`/`fold`/`asResult` in `core/src/test/kotlin/com/mirabilis/core/result/`
- [ ] T009 [P] `:core-ui` — port MVI base (`MVIViewModel`, `Reducer`, `UiState`/`UiEvent`/`UiEffect`/`UiIntent`) from `mvi-sample/` with ADR-0003 fixes (`setEvent`/`setEffect` `protected`, `History` removed) in `core-ui/src/main/kotlin/com/mirabilis/core/ui/mvi/`
- [ ] T010 [P] `:core-ui` — unit test for reducer state emission + intent handling in `core-ui/src/test/kotlin/com/mirabilis/core/ui/mvi/`
- [ ] T011 [P] `:domain` — models `PhoneVerificationChallenge`, `AuthSession`, `User` in `domain/src/main/kotlin/com/mirabilis/domain/auth/model/`
- [ ] T012 [P] `:domain` — repository interfaces `IAuthRepository`, `ISessionRepository` in `domain/src/main/kotlin/com/mirabilis/domain/auth/repository/`
- [ ] T013 [P] `:data` — `@Serializable` remote models (`OtpRequestResponseRemote`, `VerifyResponseRemote`, `RefreshResponseRemote`, `UserRemote`) in `data/src/main/kotlin/com/mirabilis/data/auth/network/`
- [ ] T014 [P] `:data` — Retrofit `AuthApi` (`POST otp/request`, `POST otp/verify`, `POST auth/refresh`, `GET /me`) per [contracts/auth-api.md](./contracts/auth-api.md) in `data/src/main/kotlin/com/mirabilis/data/auth/network/AuthApi.kt`
- [ ] T015 [P] `:data` — `SafeRemoteDataSource` + `SafeLocalDataSource` bases (try/catch → typed `AppError`) in `data/src/main/kotlin/com/mirabilis/data/auth/network/` and `.../datastore/`
- [ ] T016 [P] `:data` — `session.proto` + `user.proto` schemas in `data/src/main/proto/`
- [ ] T017 `:data` — `SessionProto`/`UserProto` `Serializer`s with Android-Keystore encryption at rest in `data/src/main/kotlin/com/mirabilis/data/auth/datastore/` — depends on T016
- [ ] T018 `:data` — mappers (`*Remote.toDomain()`, `AuthSession.toProto()`/`SessionProto.toDomain()`, `User.toProto()`/`UserProto.toDomain()`) in `data/src/main/kotlin/com/mirabilis/data/auth/mapper/` — depends on T011, T013, T016
- [ ] T019 `:data` — `SessionLocalDataSource` (DataStore read/write + map) + `@Singleton SessionHolder` in-memory token cache in `data/src/main/kotlin/com/mirabilis/data/auth/datastore/` and `.../session/` — depends on T017, T018
- [ ] T020 `:data` — `AuthInterceptor` (attach `Bearer`, skip `otp/*`/`auth/refresh` via no-auth marker) in `data/src/main/kotlin/com/mirabilis/data/auth/network/interceptor/` — depends on T019
- [ ] T021 `:data` — `AuthRemoteDataSource` impl (extends `SafeRemoteDataSource`) in `data/src/main/kotlin/com/mirabilis/data/auth/network/` — depends on T014, T015
- [ ] T022 `:data` — `FakeAuthApi` backing the running app (request/verify/me happy path) in `data/src/main/kotlin/com/mirabilis/data/auth/network/FakeAuthApi.kt` — depends on T014
- [ ] T023 `:data` — Hilt `RepositoriesModule` + `DataSourcesModule` (`@Binds` interface→impl) in `data/src/main/kotlin/com/mirabilis/data/auth/di/` — depends on T012
- [ ] T024 `:app` — `@HiltAndroidApp MirabilisApp` + `MainActivity` + Compose `NavHost` scaffold in `app/src/main/kotlin/com/mirabilis/app/`
- [ ] T025 `:app` — Hilt `DispatchersModule`, `DataStoreModule` (DataStore instances), `NetworkModule` (authed OkHttp + Retrofit with `AuthInterceptor` + kotlinx.serialization converter) in `app/src/main/kotlin/com/mirabilis/app/di/` — depends on T020, T017
- [ ] T026 `:feature:auth` — navigation graph skeleton (`SendPhone`/`VerifyPhone`/`Home` routes, `AuthNavGraph`) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/navigation/`

**Checkpoint**: Foundation ready — user story work can begin.

---

## Phase 3: User Story 1 - Sign in with a phone number via one-time code (Priority: P1) 🎯 MVP

**Goal**: Enter phone → request code → enter correct code → reach authenticated Home showing the user.

**Independent Test**: Valid phone → request → correct code → assert Home authenticated with user
info shown (quickstart scenarios 1–5).

### Tests for User Story 1 (write FIRST, ensure they FAIL) ⚠️

- [ ] T027 [P] [US1] MockWebServer contract tests for `POST /otp/request` & `POST /otp/verify` incl. AppError mapping (400/410/429) in `data/src/test/kotlin/com/mirabilis/data/auth/network/AuthApiTest.kt`
- [ ] T028 [P] [US1] Unit test `RequestOtpUseCase` (E.164 validation reject/accept) in `domain/src/test/kotlin/com/mirabilis/domain/auth/usecase/RequestOtpUseCaseTest.kt`
- [ ] T029 [P] [US1] Unit test `VerifyOtpUseCase` (success establishes + persists session) in `domain/src/test/kotlin/com/mirabilis/domain/auth/usecase/VerifyOtpUseCaseTest.kt`
- [ ] T030 [P] [US1] ViewModel tests (Turbine) for SendPhone/VerifyPhone (incl. resend cooldown — FR-005)/Home in `feature/auth/src/test/kotlin/com/mirabilis/feature/auth/`

### Implementation for User Story 1

- [ ] T031 [P] [US1] `RequestOtpUseCase` (validate phone → `requestOtp`) in `domain/src/main/kotlin/com/mirabilis/domain/auth/usecase/`
- [ ] T032 [P] [US1] `VerifyOtpUseCase` in `domain/src/main/kotlin/com/mirabilis/domain/auth/usecase/`
- [ ] T033 [P] [US1] `GetCurrentUserUseCase` (protected `GET /me`) in `domain/src/main/kotlin/com/mirabilis/domain/auth/usecase/`
- [ ] T034 [P] [US1] `ObserveAuthStateUseCase` in `domain/src/main/kotlin/com/mirabilis/domain/auth/usecase/`
- [ ] T035 [US1] `AuthRepository` impl (requestOtp / verifyOtp → persist session+user / currentUser), maps `*Remote → domain` in `data/src/main/kotlin/com/mirabilis/data/auth/repository/AuthRepository.kt` — depends on T019, T021, T018
- [ ] T036 [US1] `SessionRepository` impl (`observeAuthState`, `observeUser`) in `data/src/main/kotlin/com/mirabilis/data/auth/repository/SessionRepository.kt` — depends on T019
- [ ] T037 [P] [US1] `SendPhoneViewModel` + `UiState/Intent/Event/Effect` in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/sendphone/`
- [ ] T038 [P] [US1] `VerifyPhoneViewModel` (holds transient challenge in VM state only — FR-013) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/verifyphone/`
- [ ] T039 [P] [US1] `HomeViewModel` in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/`
- [ ] T040 [P] [US1] `SendPhoneScreen` Compose UI (phone input + inline format error — FR-002) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/sendphone/`
- [ ] T041 [P] [US1] `VerifyPhoneScreen` Compose UI (code input + retryable error — FR-004) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/verifyphone/`
- [ ] T041a [US1] Resend-code action: `Resend` intent + cooldown state in `VerifyPhoneViewModel` (reuses `RequestOtpUseCase`) and a resend button + cooldown/rate-limit message in `VerifyPhoneScreen` (FR-005; 429 → non-sensitive message) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/verifyphone/` — depends on T031, T038, T041
- [ ] T042 [P] [US1] `HomeScreen` Compose UI (user profile — FR-012) in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/`
- [ ] T043 [US1] Wire nav SendPhone→VerifyPhone→Home; connect ViewModels to use cases; `FakeAuthApi` returns a session on verify — depends on T026, T035–T042

**Checkpoint**: User Story 1 fully functional and independently testable (MVP).

---

## Phase 4: User Story 2 - Stay signed in without re-entering a code (Priority: P2)

**Goal**: Session persists across restarts; expired access token is refreshed transparently on a
protected call; concurrent 401s refresh exactly once.

**Independent Test**: Reopen app with valid session → Home, no code prompt; force access expiry →
protected action succeeds with no visible re-auth; concurrent 401s → one refresh (quickstart 7–9).

### Tests for User Story 2 (write FIRST, ensure they FAIL) ⚠️

- [ ] T044 [P] [US2] MockWebServer test: `GET /me` `401` → `/auth/refresh` → retry succeeds in `data/src/test/kotlin/com/mirabilis/data/auth/network/TokenAuthenticatorTest.kt`
- [ ] T045 [P] [US2] MockWebServer test: concurrent 401s trigger exactly ONE `/auth/refresh` (single-flight) in `data/src/test/kotlin/com/mirabilis/data/auth/network/TokenAuthenticatorConcurrencyTest.kt`
- [ ] T046 [P] [US2] Test: stored session restored on restart → `observeAuthState` emits `true` in `data/src/test/kotlin/com/mirabilis/data/auth/repository/SessionRepositoryTest.kt`

### Implementation for User Story 2

- [ ] T047 [US2] `TokenAuthenticator` (single-flight `Mutex`, token re-check, anti-loop separate client, bounded retries via `priorResponse`) in `data/src/main/kotlin/com/mirabilis/data/auth/network/interceptor/` — depends on T019
- [ ] T048 [US2] `refresh(refreshToken)` path in `AuthRepository` (persist new tokens via `SessionLocalDataSource`) in `data/src/main/kotlin/com/mirabilis/data/auth/repository/AuthRepository.kt` — depends on T035
- [ ] T049 [US2] `:app` `NetworkModule` — add refresh-only OkHttp/Retrofit client (no `AuthInterceptor`/authenticator) and attach `TokenAuthenticator` to the authed client — depends on T025, T047
- [ ] T050 [US2] Extend `FakeAuthApi` + MockWebServer dispatcher for `/auth/refresh` and an expiring access token in `data/src/main/kotlin/com/mirabilis/data/auth/network/FakeAuthApi.kt` — depends on T022
- [ ] T051 [US2] App-restart routing: top-level `NavHost` observes `ObserveAuthStateUseCase` → Home without code prompt — depends on T034, T036, T024

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 5: User Story 3 - Graceful session end (Priority: P3)

**Goal**: Sign-out or a non-renewable session clears credentials and returns the user to SendPhone;
reopening requires a fresh sign-in.

**Independent Test**: Trigger sign-out (or simulate non-renewable session) → assert SendPhone and
protected data no longer accessible; reopen requires sign-in (quickstart 10–11).

### Tests for User Story 3 (write FIRST, ensure they FAIL) ⚠️

- [ ] T052 [P] [US3] Test `SignOutUseCase` clears session+user → `observeAuthState` emits `false` in `domain/src/test/kotlin/com/mirabilis/domain/auth/usecase/SignOutUseCaseTest.kt`
- [ ] T053 [P] [US3] Test non-renewable session: `/auth/refresh` `401` → `TokenAuthenticator` returns `null` → session cleared → route SendPhone in `data/src/test/kotlin/com/mirabilis/data/auth/network/TokenAuthenticatorLogoutTest.kt`

### Implementation for User Story 3

- [ ] T054 [P] [US3] `SignOutUseCase` in `domain/src/main/kotlin/com/mirabilis/domain/auth/usecase/`
- [ ] T055 [US3] `SessionRepository.signOut()`/`clearSession()` impl (clear DataStore + `SessionHolder`) in `data/src/main/kotlin/com/mirabilis/data/auth/repository/SessionRepository.kt` — depends on T036, T019
- [ ] T056 [US3] `TokenAuthenticator` give-up path → `clearSession()` → `authState=false` in `data/src/main/kotlin/com/mirabilis/data/auth/network/interceptor/` — depends on T047, T055
- [ ] T057 [US3] Home sign-out action wired; routing returns to SendPhone on `authState=false` in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/` + navigation — depends on T054, T051

**Checkpoint**: All three user stories independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T058 [P] Run every [quickstart.md](./quickstart.md) scenario (1–14) and confirm pass
- [ ] T059 [P] Purity guard: assert `:domain`/`:core` have no Android/Retrofit/Room imports (compile check or Konsist test) in `domain/src/test/` / `core/src/test/`
- [ ] T060 [P] `AppError` → user-facing, non-sensitive message mapping incl. backend rate-limit/lockout (FR-014) in `feature/auth/`
- [ ] T061 [P] ktlint/detekt clean; cross-link ADRs/spec in module READMEs
- [ ] T062 Record provenance (spec id, skill, agent/model) per constitution Principle IV

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies — start immediately.
- **Foundational (Phase 2)**: depends on Setup — **BLOCKS all user stories**.
- **User Stories (Phase 3–5)**: all depend on Foundational. US1 has no dependency on US2/US3. US2
  and US3 build on foundational infra and US1's repositories/routing but keep independent test seams.
- **Polish (Phase 6)**: after the desired stories are complete.

### Story-level dependencies

- **US1 (P1)**: after Foundational. Self-contained MVP.
- **US2 (P2)**: after Foundational; reuses `AuthRepository`/routing from US1 (T035, T036) but its
  refresh path (T047–T050) is independently testable via MockWebServer.
- **US3 (P3)**: after Foundational; `clearSession` (T055) reuses US2's `TokenAuthenticator` (T047)
  for the non-renewable path, and US1/US2 routing for return-to-SendPhone.

### Within each story

- Tests (T027–T030, T044–T046, T052–T053) written first and failing before implementation.
- Domain use cases → repository impls → ViewModels → Compose UI → wiring.

---

## Parallel Opportunities

- Setup: T002, T004, T005 in parallel.
- Foundational: T006, T007, T008, T009, T010, T011, T012, T013, T014, T015, T016 in parallel
  (different files); T017–T026 follow their noted deps.
- US1 tests T027–T030 in parallel; use cases T031–T034 in parallel; UI T040–T042 in parallel.
- US2 tests T044–T046 in parallel. US3 tests T052–T053 in parallel.

### Parallel Example: User Story 1

```bash
# Failing tests first (parallel):
Task: "Unit test RequestOtpUseCase in domain/src/test/.../RequestOtpUseCaseTest.kt"
Task: "Unit test VerifyOtpUseCase in domain/src/test/.../VerifyOtpUseCaseTest.kt"
Task: "MockWebServer contract tests for otp/request & otp/verify in data/src/test/.../AuthApiTest.kt"
Task: "ViewModel tests (Turbine) in feature/auth/src/test/..."

# Then use cases (parallel):
Task: "RequestOtpUseCase in domain/.../usecase/"
Task: "VerifyOtpUseCase in domain/.../usecase/"
Task: "GetCurrentUserUseCase in domain/.../usecase/"
Task: "ObserveAuthStateUseCase in domain/.../usecase/"
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1 Setup → 2. Phase 2 Foundational → 3. Phase 3 US1 → **STOP & VALIDATE** (quickstart 1–5)
→ demo passwordless sign-in.

### Incremental Delivery

1. Setup + Foundational → foundation ready.
2. US1 → test → demo (MVP: sign in to Home).
3. US2 → test → demo (persistence + transparent refresh).
4. US3 → test → demo (sign-out + graceful teardown).

### Notes

- `[P]` = different files, no incomplete-task deps.
- The verification challenge stays in ViewModel state only — never persisted (FR-013).
- Single-flight/anti-loop refresh (T047) is the subtle piece — cover with MockWebServer (T044/T045).
- Green CI across all module test tasks = feature done (constitution Principle III).