# Tasks: Confirm Sign-Out

**Input**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md),
[contracts/home-signout-ui.md](./contracts/home-signout-ui.md), [quickstart.md](./quickstart.md)

**Governing decisions (ADRs)**: [ADR-0003](../../decisions/ADR-0003-android-architecture-clean-mvi.md)
(Clean Architecture + MVI), [ADR-0004](../../decisions/ADR-0004-dependency-injection-hilt.md) (Hilt),
[ADR-0010](../../decisions/ADR-0010-automated-provenance-and-metrics.md) (provenance). Implement WITHIN
these; a task that would conflict with an ADR is a human `architecture-change` gate — stop and escalate.

**Tests**: INCLUDED, tests-first (Constitution Principle III). Package base `com.mirabilis`.
**Reuse (do NOT rebuild)**: `SignOutUseCase` + `ObserveAuthStateUseCase` + `HomeEffect.NavigateToSendPhone`
from 001; the `:core-ui` MVI base; the Home surface hosted by `NavShell` from 003. Material3 `AlertDialog`
is already available — **no new dependency**.

## Phase 1: Setup

- [ ] T001 Confirm reuse + no new module/dependency: change is confined to `:feature:auth/home`; reuse `SignOutUseCase` (001) and Material3 `AlertDialog`. No `settings.gradle.kts` / `libs.versions.toml` / `build.gradle.kts` edit (if one seems needed → STOP, human `dependency-add` gate).

## Phase 2: Foundational (blocking)

- [ ] T002 Extend the Home MVI contract in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/HomeViewModel.kt`: add `showSignOutConfirm: Boolean = false` and `signOutError: String? = null` to `HomeUiState`; replace `HomeIntent.SignOut` with `SignOutRequested`, `SignOutConfirmed`, `SignOutCancelled`; reduce the visibility flag. No sign-out call yet. (see ADR-0003)

## Phase 3: US1 — Confirm before signing out (P1) 🎯 MVP

**Goal**: tapping Sign out asks for confirmation; confirming ends the session and routes to sign-in.
**Independent test**: from signed-in, `SignOutRequested` shows the dialog without signing out; `SignOutConfirmed` signs out.

- [ ] T003 [P] [US1] Turbine test: `SignOutRequested` sets `showSignOutConfirm=true` and does NOT invoke `SignOutUseCase`, in `feature/auth/src/test/kotlin/com/mirabilis/feature/auth/home/HomeViewModelTest.kt`
- [ ] T004 [P] [US1] Turbine test: `SignOutConfirmed` invokes `SignOutUseCase`; on success the `HomeEffect.NavigateToSendPhone` effect is emitted (same file)
- [ ] T005 [P] [US1] Turbine test: `SignOutConfirmed` when `SignOutUseCase` returns `Result.Error` → user unchanged, `showSignOutConfirm=false`, `signOutError` set, NO `NavigateToSendPhone` (FR-006) (same file)
- [ ] T006 [US1] Implement `SignOutRequested`/`SignOutConfirmed` in `HomeViewModel` incl. failure handling (reuse `SignOutUseCase` + `toUserMessage()`; success routes via the existing auth-state observer) — depends T002; turns T003–T005 green. (see ADR-0003/0004)
- [ ] T007 [US1] `HomeScreen` in `feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/HomeScreen.kt`: "Sign out" button → `SignOutRequested`; render `AlertDialog` when `showSignOutConfirm`, confirm action → `SignOutConfirmed`, copy states the user will need to sign in again (FR-005/C6); surface `signOutError` when set.

## Phase 4: US2 — Cancel and stay signed in (P2)

**Goal**: cancelling/dismissing keeps the user signed in, on the same screen, no state lost.
**Independent test**: with the dialog open, `SignOutCancelled` closes it and the session is unchanged.

- [ ] T008 [P] [US2] Turbine test: `SignOutCancelled` clears `showSignOutConfirm`, does NOT invoke `SignOutUseCase`, user unchanged, in `HomeViewModelTest.kt`
- [ ] T009 [US2] Implement `SignOutCancelled` in `HomeViewModel` (`showSignOutConfirm=false`, no session change) — depends T002
- [ ] T010 [US2] `HomeScreen` dialog: wire the Cancel action and `onDismissRequest` (tap-outside / back) both to `SignOutCancelled` (FR-004: dismiss keeps session).

## Phase 5: Polish

- [ ] T011 [P] Run [quickstart.md](./quickstart.md) JVM scenarios 1–6; `:feature:auth:test` + `detekt` green.
- [ ] T012 Record provenance (spec 004, method, agent/model) per Principle IV / [ADR-0010](../../decisions/ADR-0010-automated-provenance-and-metrics.md).

## Dependencies

- Setup (T001) → Foundational (T002) → US1 (T003–T007) → US2 (T008–T010) → Polish (T011–T012).
- **US2 depends only on Foundational T002** (shared `showSignOutConfirm` state), not on US1 — the two
  stories are otherwise independent and independently testable.
- Within US1: tests T003–T005 are written first (they fail), then T006 implements, then T007 wires UI.

## MVP

Phase 1 + 2 + **US1 (T003–T007)** = tapping Sign out asks to confirm and only then ends the session.
Add US2 (cancel/dismiss safety) next.

## Parallel opportunities

- T003, T004, T005 ([P], same test file but independent cases) can be authored together.
- T008 ([P]) can be authored alongside the US1 tests once T002 exists.
