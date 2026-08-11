# Implementation Plan: Confirm Sign-Out

**Branch**: `004-confirm-sign-out` | **Date**: 2026-08-10 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-confirm-sign-out/spec.md`

## Summary

Gate the existing sign-out action behind a confirmation dialog. Today `HomeScreen`'s "Sign out"
button dispatches `HomeIntent.SignOut`, which immediately calls `SignOutUseCase` (spec 001). This
feature inserts a confirmation step: the button *requests* sign-out (opens a dialog); confirming runs
the existing use case; cancelling/dismissing closes the dialog with no session change. On sign-out
failure the user stays signed in with a recoverable message. Entirely within `:feature:auth/home` —
no new domain/data, no new dependency; reuses `SignOutUseCase` (001) and the existing Home surface
hosted by `NavShell` (003).

## Technical Context

**Language/Version**: Kotlin (JDK 21 in CI), Android `compileSdk 36`, `minSdk 26`

**Primary Dependencies**: Jetpack Compose Material3 (`AlertDialog` — already in the catalog), Hilt +
hilt-navigation-compose, the `:core-ui` MVI base — all already present. **No new dependency.**

**Storage**: N/A — no new persisted data (confirmation is transient UI state; session is owned by 001).

**Testing**: JVM unit tests — JUnit4 + Turbine + kotlinx-coroutines-test, extending the existing
`HomeViewModelTest`. Dialog is a thin Compose surface driven by ViewModel state (behavior is asserted
at the ViewModel/state level; no new instrumented test required).

**Target Platform**: Android API 26+

**Project Type**: mobile-app (multi-module Clean Architecture)

**Performance Goals**: N/A (UI interaction; dialog open/confirm/cancel are instantaneous)

**Constraints**: Confirmation state must survive configuration change (dialog stays open on rotation);
no session change on any path except explicit confirm.

**Scale/Scope**: 1 module (`:feature:auth`), 1 screen (`HomeScreen` + `HomeViewModel`), 1 dialog.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Spec-first, verifiable**: ✅ spec approved, acceptance scenarios become tests.
- **II. Small, verifiable units (SOLID)**: ✅ change confined to `HomeViewModel`/`HomeScreen`; adds
  intents + one boolean state field; no new module.
- **III. Tests as executable spec (NON-NEGOTIABLE)**: ✅ Turbine tests written first for the new
  intents (request/confirm/cancel, failure path).
- **IV. Knowledge in git + provenance**: ✅ spec/plan/tasks in git; provenance trailers on commit.
- **V. Neutral core, pluggable adapters**: ✅ no tool coupling; reuses domain `SignOutUseCase`.
- **Mandatory human gates**: no `dependency-add` (Material3 `AlertDialog` already available), no
  `architecture-change` (stays within ADR-0003 Clean+MVI, ADR-0004 Hilt). Nothing to escalate.

**Result**: PASS — no violations, Complexity Tracking not required.

## Project Structure

### Documentation (this feature)

```text
specs/004-confirm-sign-out/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (UI state shape — no persisted entities)
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (UI interaction contract)
└── tasks.md             # Phase 2 (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
feature/auth/src/main/kotlin/com/mirabilis/feature/auth/home/
├── HomeViewModel.kt     # + SignOutRequested/Confirmed/Cancelled intents, showSignOutConfirm +
│                        #   signOutError state, confirm handles SignOutUseCase failure (FR-006)
└── HomeScreen.kt        # "Sign out" button → SignOutRequested; AlertDialog on showSignOutConfirm

feature/auth/src/test/kotlin/com/mirabilis/feature/auth/home/
└── HomeViewModelTest.kt # + tests: request opens dialog (no sign-out), confirm signs out,
                         #   cancel keeps session, failure keeps session + surfaces error
```

**Structure Decision**: Single-module change inside `:feature:auth` (the Home surface). No changes to
`:domain`, `:data`, `:feature:navigation`, or `:app`. The Sign out entry point exists only in
`HomeScreen` (hosted by `NavShell`), so covering it there satisfies FR-007 for every surface.

## Complexity Tracking

> Not applicable — Constitution Check passed with no violations.
