# Phase 0 Research: Design System

**Feature**: 005-design-system | **Date**: 2026-08-11

All items below resolve the Technical Context; there were no open `NEEDS CLARIFICATION` markers
(the spec's Clarifications session already fixed typeface, theming, and neutral/semantic strategy).

---

## D1 — Module placement: new `:core-designsystem` vs. fold into `:core-ui`

**Decision**: Add a new leaf Android library module `:core-designsystem`
(namespace `com.mirabilis.core.designsystem`).

**Rationale**:
- `:core-ui` today is the **pure MVI base** (`MVIViewModel`, `Reducer`, `Contracts`) and is
  intentionally *not* Compose-enabled. Mixing a Compose component library into it muddies that
  contract and forces MVI consumers to pull Compose.
- The repo already modularizes by layer (`core`, `core-ui`, `domain`, `data`, `feature/*`); a
  dedicated design-system core module matches that pattern and gives feature modules **one
  explicit reuse target** for tokens + components.
- Leaf module: depends on nothing internal → dependency arrows point inward (Principle II).

**Alternatives considered**:
- *Fold into `:core-ui`* — rejected: couples MVI base to Compose, weaker contract.
- *Put components in `:app`* — rejected: not reusable by feature modules; violates layering.

**Gate note**: Adding a module follows the existing architecture; it is **not** an
architecture-change (no ADR contradicted/created) and adds **no** third-party dependency (all
Compose/Material artifacts already in `libs.versions.toml`). Normal human review at merge applies.

---

## D2 — Compose enablement without new dependencies

**Decision**: Enable Compose on `:core-designsystem` via the existing catalog entries only:
`kotlin-compose` plugin, `androidx-compose-bom`, `androidx-compose-ui`,
`androidx-compose-material3`, `androidx-compose-ui-tooling-preview`, and
`debugImplementation(androidx-compose-ui-tooling)` for previews — mirroring `:feature:auth`'s
build file.

**Rationale**: `gradle/libs.versions.toml` already pins Compose BOM `2025.09.00` + Material 3.
Reusing them keeps determinism (Principle: pinned versions) and avoids the dependency-add gate.

**Alternatives considered**: Compose Material (M2) — rejected; the app is already on Material 3.

---

## D3 — Brand color role mapping (measured against WCAG)

Computed relative luminance (sRGB) and contrast for the 5 greens vs. white/black:

| Color | Hex | Luminance | vs. white | vs. black | Usable as |
|---|---|---|---|---|---|
| Evergreen | `#11270b` | 0.016 | **15.9:1** ✅ | 1.32 | darkest surface / on-light-green text / dark-theme bg |
| Olive Leaf | `#3c5a14` | 0.083 | **7.88:1** ✅ | 2.66 | **light-theme primary** (white text) |
| Forest Moss | `#598b2c` | 0.208 | 4.07 ❌ | 5.15 ✅ | container / large-text only (fails white body text) |
| Sage Green | `#669d31` | 0.272 | 3.26 | **6.43:1** ✅ | secondary (dark text) / light container |
| Bright Fern | `#71b340` | 0.361 | 2.55 | **8.22:1** ✅ | **dark-theme primary** (dark text) / light accent |

**Decision** (seed role assignment; final container/on-* tones tuned to pass the contrast test):

- **Light theme**: `primary` = Olive Leaf `#3c5a14` / `onPrimary` = white; `secondary` = Sage
  Green `#669d31` (dark `onSecondary`); `tertiary` = Bright Fern `#71b340` (dark `onTertiary`);
  `primaryContainer` = Bright Fern / `onPrimaryContainer` = Evergreen `#11270b`.
- **Dark theme**: `primary` = Bright Fern `#71b340` / `onPrimary` = Evergreen `#11270b`;
  surfaces near Evergreen; `secondary` = Sage Green; containers = Forest Moss/Olive Leaf with
  light on-colors.

**Rationale**: Filled primaries must clear AA for body text — only Olive Leaf/Evergreen do that
with white, so light-theme primary is Olive Leaf; dark theme needs a *light* primary with dark
text, which Bright Fern provides (8.22:1). Forest Moss (white 4.07) is confined to containers /
large text where 3:1 suffices.

**Alternatives considered**: Forest Moss or Bright Fern as light-theme primary — rejected
(white-on fails AA body). Generating a full HCT tonal palette per seed (Material dynamic color) —
deferred as unnecessary; a hand-tuned role set that passes the contrast test is simpler and
deterministic.

---

## D4 — Derived neutrals & semantic colors (palette has none)

**Decision**: Derive a **9-step neutral ramp** (a near-white green-tinted `surface` through a
near-black Evergreen-tinted `onSurface`) for surfaces, text, borders, dividers, disabled. Add
standard semantic colors: **error** (Material `#B3261E` light / `#F2B8B5` dark), **warning**
(`#8A5A00` light / `#E9B949` dark), **success** (a teal-shifted green, e.g. `#2E7D5B` light /
`#7FD1AE` dark, chosen to stay distinguishable from the brand greens).

**Rationale**: FR-003/FR-004 require neutrals + semantics the palette lacks; Material conventions
give accessible, familiar defaults. Success is deliberately shifted away from the brand greens so
status is not confused with brand chrome; status is always reinforced with icon + text, never
color alone (accessibility).

**Alternatives considered**: Reuse a brand green for success — rejected (indistinguishable from
primary chrome / color-only signalling).

---

## D5 — Typography

**Decision**: Roboto-based Material 3 type scale (`display/headline/title/body/label`, each
large/medium/small) declared in `Type.kt`, no bundled font file (Roboto is the platform default).

**Rationale**: Matches the spec clarification; zero font asset/licensing overhead; swappable later
by editing one file.

---

## D6 — Per-component preview strategy (FR-019 / SC-007)

**Decision**: Co-locate `@Preview` functions with each component, one for light and one for dark
(`@Preview(uiMode = UI_MODE_NIGHT_YES)`), each wrapping the component in `MirabilisTheme`. A
`ComponentCatalog.kt` aggregates all components into a single scrollable gallery surface (FR-017)
usable both as a `@Preview` and as a screen the app *could* host later (not wired here).

**Rationale**: `@Preview` needs no new dependency and gives designers/devs immediate light+dark
review at design time. The catalog doubles as living documentation.

---

## D7 — Testing strategy (Principle III) & visual-regression deferral

**Decision**:
- **JVM unit tests (authoritative acceptance):**
  - `ContrastTest` — recompute WCAG contrast for **every** offered foreground/background token
    pairing in both themes; assert ≥ 4.5:1 (text) / ≥ 3:1 (large text & UI components). Enforces
    FR-015 / SC-003.
  - `TokenCompletenessTest` — assert every color role has a value in **both** light and dark
    schemes (FR-005 / SC-002) and every typography role is defined.
- **Optional instrumented tests** using `ui-test-junit4` (already in catalog) for component state
  semantics (e.g. disabled button not clickable) — added if cheap.
- **Screenshot / visual-regression testing: DEFERRED** per
  [ADR-0011](../../decisions/ADR-0011-visual-regression-testing-deferred.md). Paparazzi/Roborazzi
  are **not** in the catalog → would trigger the dependency-add gate. `@Preview` (D6) is the
  interim manual safeguard.

**Rationale**: Contrast and token completeness — the spec's core quality claims — are perfectly
expressible as deterministic JVM tests with **no device and no new dependency**, satisfying the
NON-NEGOTIABLE test principle while honoring ADR-0011.

**Alternatives considered**: Make screenshot tests part of this feature — rejected (new dependency
+ contradicts ADR-0011). Manual-only verification — rejected (fails Principle III).

---

## D8 — Relationship to the existing app theme

**Decision**: `MirabilisTheme` replaces the ad-hoc `MaterialTheme(lightColorScheme()/
darkColorScheme())` currently inlined in `MainActivity`, but **this feature does not edit
`MainActivity`** (FR-018). The existing `ThemeViewModel` (System/Light/Dark preference, Proto
DataStore) is the intended driver and is **reused as-is** when a later feature adopts the design
system.

**Rationale**: Keeps scope to creating the system; adoption (swapping `MaterialTheme` →
`MirabilisTheme` in `:app`) is a separate, reviewable change owned by an integration spec.
