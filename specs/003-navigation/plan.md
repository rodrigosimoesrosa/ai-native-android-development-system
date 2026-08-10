# Implementation Plan: App Navigation Shell

**Branch**: `003-navigation` | **Date**: 2026-08-10 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-navigation/spec.md`

## Summary

An authenticated **navigation shell** (bottom navigation) that hosts the app's top-level destinations
— **Home** (from 001) and **Profile** (from 002) — letting a signed-in user switch between them while
**preserving each destination's state**. It **hosts** existing screens (does not rebuild them),
composes with the existing top-level auth-state routing (`AuthRoot`), and depends on Profile only for
its **route id**. Inherited architecture (ADR-0003): Compose Navigation + MVI where stateful; new
`:feature:navigation` module. Almost no domain/data — this is a presentation/navigation feature.

## Technical Context

**Language/Version**: Kotlin `2.2.20` (inherited). **Primary Dependencies**: Compose + Navigation
Compose + Material3, Hilt (all pinned). **Storage**: none (navigation is stateless beyond nav state).
**Testing**: JVM unit tests for the small stateful parts (destination list, auth-gating decision);
Compose UI / nav behavior is instrumented (`connectedAndroidTest`). **Target**: Android
`minSdk 26`/`compileSdk 36`. **Project Type**: mobile (multi-module). **Constraints**: shown only when
authenticated; hosts existing destinations without editing them. **Scale/Scope**: 1 shell, 2 initial
destinations.

## Constitution Check

| # | Principle | Status |
|---|-----------|--------|
| I | Specs are source of truth | PASS |
| II | Small units, explicit boundaries | PASS — new `:feature:navigation`; hosts others via their public route/composable |
| III | Tests as executable spec | PASS — JVM tests for gating/destinations; nav behavior via instrumented tests |
| IV | Knowledge in git + provenance | PASS |
| V | Neutral core, boring tech | PASS — standard Navigation Compose; no new deps |
| — | Reuse over duplication | PASS — hosts Home (001) + Profile (002); rebuilds neither |

**Result: PASS.** No new deps, no architecture change → Complexity Tracking empty.

## Project Structure

```text
specs/003-navigation/  brief.md · spec.md · plan.md · research.md · data-model.md · quickstart.md · tasks.md

feature/navigation/  Destination.kt (route id + label + icon), NavShell.kt (Scaffold + NavigationBar +
                     inner NavHost hosting Home + Profile, state-preserving), NavShellViewModel (if any
                     stateful gating), navigation graph entry
app/                 AuthRoot: when Authenticated → NavShell (instead of AuthNavGraph HOME); wiring
feature/auth/        (reused) Home composable + route; auth-state routing already exists
feature/profile/     (reused) Profile route id `profile` (from 002 — the shared interface)
```

**Structure Decision**: New `:feature:navigation` depends on `:feature:auth` (Home) and
`:feature:profile` (Profile route). It plugs **into** the existing `AuthRoot` authenticated area
(001/US2), replacing the single Home destination with the shell. The only cross-feature seam to 002 is
the `profile` route id — resolved at merge.

## Complexity Tracking

> No Constitution Check violations — intentionally empty.
