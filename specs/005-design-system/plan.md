# Implementation Plan: Design System (UI Component Source of Truth)

**Branch**: `005-design-system` | **Date**: 2026-08-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-design-system/spec.md`

## Summary

Build the app's design system as a reusable core layer: brand **color tokens** (the 5 greens +
derived neutral ramp + semantic error/warning/success), a **Roboto typography scale**, **shape /
spacing / elevation tokens**, a **`MirabilisTheme`** wrapper over Material 3, and a catalog of
**stateless wrapper components** (buttons, text, text fields, FABs, plus switches, checkboxes,
radio buttons, cards, chips, top app bar, dialogs). Every component sources appearance only from
tokens, ships an isolated `@Preview` in light + dark, and is verified by JVM contrast tests
(WCAG AA). Screen integration is **out of scope** (FR-018).

Technical approach: a new `:core-designsystem` Android library module hosting theme + components,
Compose-enabled using dependencies **already in the version catalog** (Compose BOM, Material 3,
ui-tooling-preview) — so **no dependency-add gate is triggered**. Automated visual-regression
(screenshot) testing stays **deferred** per [ADR-0011](../../decisions/ADR-0011-visual-regression-testing-deferred.md);
`@Preview` is the manual foundation, and machine-checkable AA contrast is enforced by unit tests.

## Technical Context

**Language/Version**: Kotlin 2.2.20 (JVM toolchain 17)

**Primary Dependencies**: Jetpack Compose (BOM 2025.09.00), Material 3, Compose ui-tooling-preview
— all already pinned in `gradle/libs.versions.toml`. No new third-party dependency.

**Storage**: N/A (design tokens are code constants; theme *preference* persistence already exists
in `:app` via `ThemeViewModel` + Proto DataStore — reused, not modified here).

**Testing**: JUnit4 (JVM) for token/contrast tests; Compose `ui-test-junit4` (already in catalog)
available for optional instrumented state tests. Screenshot testing deferred (ADR-0011).

**Target Platform**: Android, minSdk 26 / compileSdk 36 / targetSdk 36.

**Project Type**: Mobile app (multi-module Android), adding one core library module.

**Performance Goals**: Components are stateless, allocation-light composables; no measurable
runtime overhead beyond stock Material 3.

**Constraints**: Every offered fg/bg token pairing meets WCAG AA (FR-015); text respects system
font scale (FR-016); light + dark parity (FR-005); no hard-coded color/font/spacing in components
(FR-012).

**Scale/Scope**: ~4 core component families (P1) + 7 extended controls (P2), one token layer, one
`MirabilisTheme`, one catalog/gallery surface. No screens integrated.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Gate | Status | Notes |
|---|---|---|
| I. Specs are source of truth | ✅ | Approved `spec.md` (005) with FR/SC drives this plan. |
| II. Small, verifiable units + SOLID / Dependency Inversion | ✅ | Design system is a **leaf** core module: no `:domain`/`:data` deps, dependency arrows point inward toward it. Components are small, contract-first (see `contracts/`). |
| III. Tests as executable spec (NON-NEGOTIABLE) | ✅ | AA contrast + token-completeness expressed as **JVM unit tests** (deterministic, no device). Test-first. |
| IV. Knowledge in git + provenance | ✅ | All tokens/decisions live in this feature's markdown + code; commit trailers per ADR-0010. |
| V. Neutral core, tools as adapters | ✅ (n/a) | App-layer feature; no tool-adapter logic involved. |
| Architecture baseline (ADR-0003 Clean+MVI, ADR-0004 Hilt) | ✅ | Design system is a **stateless presentation library**: no ViewModels, no DI graph, no domain models → MVI/Hilt not applicable and correctly not introduced. |
| Gate: dependency-add | ✅ Not triggered | All Compose/Material deps already in `libs.versions.toml`. |
| Gate: architecture-change | ✅ Not triggered | Adding a core module follows the **existing** modularization-by-layer pattern (core, core-ui, domain, data); no ADR is contradicted or created. Flagged for normal human review at the merge gate. |
| Gate: screenshot/visual-regression | ✅ Honored | Kept **deferred** per ADR-0011 (would need Paparazzi/Roborazzi = new dep). `@Preview` used instead. |

**Result**: PASS — no violations, Complexity Tracking not required.

## Project Structure

### Documentation (this feature)

```text
specs/005-design-system/
├── plan.md              # This file
├── research.md          # Phase 0 — decisions (module, color mapping, testing, deferrals)
├── data-model.md        # Phase 1 — token & component catalog (roles, light/dark values)
├── quickstart.md        # Phase 1 — how to build & validate the design system
├── contracts/
│   └── ui-components.md  # Phase 1 — public composable API surface (signatures, variants, states)
└── tasks.md             # Phase 2 — created by /speckit-tasks (NOT here)
```

### Source Code (repository root)

New module `:core-designsystem` (namespace `com.mirabilis.core.designsystem`), sibling to the
existing `:core-ui` (which stays the pure MVI base). Registered in `settings.gradle.kts`.

```text
core-designsystem/
├── build.gradle.kts                         # android-library + kotlin-compose; catalog deps only
└── src/
    ├── main/kotlin/com/mirabilis/core/designsystem/
    │   ├── token/
    │   │   ├── Color.kt                      # raw palette + derived neutrals/semantics (internal)
    │   │   ├── ColorRoles.kt                 # light & dark ColorScheme role mapping
    │   │   ├── Type.kt                        # Roboto typography scale
    │   │   ├── Shape.kt                       # corner-radius tokens
    │   │   ├── Spacing.kt                     # spacing tokens
    │   │   └── Elevation.kt                   # elevation tokens
    │   ├── theme/
    │   │   └── MirabilisTheme.kt              # wraps MaterialTheme(colorScheme, typography, shapes)
    │   ├── component/
    │   │   ├── MirabilisButton.kt             # primary/secondary/text variants + states (FR-008)
    │   │   ├── MirabilisText.kt               # bound to typography + color roles (FR-009)
    │   │   ├── MirabilisTextField.kt          # label/placeholder/helper/error (FR-010)
    │   │   ├── MirabilisFab.kt                # size variants (FR-011)
    │   │   └── extended/                      # switch, checkbox, radio, card, chip, appbar, dialog (FR-013)
    │   └── catalog/
    │       └── ComponentCatalog.kt           # gallery surface: all tokens + components (FR-017)
    └── test/kotlin/com/mirabilis/core/designsystem/
        ├── ContrastTest.kt                   # WCAG AA for every offered fg/bg pairing (FR-015/SC-003)
        └── TokenCompletenessTest.kt          # every role defined for light AND dark (FR-005/SC-002)
```

Each component file also carries `@Preview` functions (light + dark) co-located with the component
(FR-019 / SC-007). Screens are **not** touched (FR-018).

**Structure Decision**: New leaf module `:core-designsystem` rather than folding into `:core-ui`.
Rationale in [research.md](research.md): keeps `:core-ui`'s pure MVI role intact, gives feature
modules a single, explicit reuse target, and matches the established core-layer modularization.
No third-party dependency added; only catalog entries are wired.

## Complexity Tracking

> Not required — Constitution Check passed with no violations.
