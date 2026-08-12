# Quickstart & Validation: Design System

**Feature**: 005-design-system | **Date**: 2026-08-11

How to build and prove the design system works end-to-end. No screens are integrated (FR-018);
validation is via unit tests + previews + the catalog surface.

## Prerequisites

- Repo builds (`./gradlew help`), JDK 17.
- Module `:core-designsystem` registered in `settings.gradle.kts` and Compose-enabled using
  **only** existing catalog entries (see [research.md](research.md) D2). No new dependency.

## Build

```bash
./gradlew :core-designsystem:assemble
```

Expected: module compiles; no `dependency-add` gate (all Compose/Material deps already pinned).

## Validate — automated (authoritative, Principle III)

```bash
./gradlew :core-designsystem:test
```

Expected:
- **ContrastTest** passes → every offered foreground/background token pairing meets WCAG AA in
  both light and dark (FR-015 / SC-003).
- **TokenCompletenessTest** passes → every color role has a light AND dark value; all 15
  typography roles defined (FR-005 / SC-002).

Detekt (quality gate, ADR-0007):

```bash
./gradlew :core-designsystem:detekt
```

## Validate — visual (design-time)

- Open each component file in the IDE → its **Light** and **Dark** `@Preview` render the component
  in all states (FR-019 / SC-007).
- Open `ComponentCatalog.kt` preview → single gallery of every token + component in both themes
  (FR-017 / SC-002). Toggle system font size to the largest setting → no clipping (SC-006).

## Acceptance mapping

| Spec item | Validated by |
|---|---|
| FR-001/002/012 tokens-only, brand greens mapped | ContrastTest + code review of components (no raw hex/dp) |
| FR-003/004 neutrals + semantics | TokenCompletenessTest; [data-model.md](data-model.md) |
| FR-005 / SC-005 light+dark parity | TokenCompletenessTest; catalog dark preview |
| FR-006 typography (Roboto) | TokenCompletenessTest; text previews |
| FR-008–011/013 components | catalog + per-component previews |
| FR-015 / SC-003 WCAG AA | ContrastTest |
| FR-016 / SC-006 font scaling | catalog preview at max font scale |
| FR-017 gallery | `ComponentCatalog` preview |
| FR-019 / SC-007 per-component previews | Light/Dark `@Preview` per component |
| FR-018 no screen integration | `MainActivity`/feature screens unchanged in this feature's diff |

## Out of scope (do not do here)

- Editing `:app` `MainActivity` to use `MirabilisTheme` (adoption = separate spec).
- Adding Paparazzi/Roborazzi screenshot tests (deferred — ADR-0011).
- Restyling existing auth/profile/navigation screens.
