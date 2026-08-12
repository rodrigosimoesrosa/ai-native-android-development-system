# Phase 1 Data Model: Design System

**Feature**: 005-design-system | **Date**: 2026-08-11

"Data" here = the **token catalog** (design constants) and the **component catalog**. No database,
no domain entities. Entities map to `spec.md` → Key Entities.

---

## Entity: Color Token (role)

A named color role with a **light** and **dark** value. Consumed via `MaterialTheme.colorScheme`
inside `MirabilisTheme`. Raw hex is never used by components (FR-001).

### Brand roles (seed values; container/on-* tuned to pass ContrastTest)

| Role | Light | Dark | Source |
|---|---|---|---|
| `primary` | `#3c5a14` Olive Leaf | `#71b340` Bright Fern | brand |
| `onPrimary` | `#FFFFFF` | `#11270b` Evergreen | derived |
| `primaryContainer` | `#71b340` Bright Fern | `#3c5a14` Olive Leaf | brand |
| `onPrimaryContainer` | `#11270b` Evergreen | `#DDEFC8` (light green) | derived |
| `secondary` | `#669d31` Sage Green | `#669d31` Sage Green | brand |
| `onSecondary` | `#11270b` Evergreen | `#11270b` Evergreen | derived |
| `tertiary` | `#598b2c` Forest Moss | `#8fc95f` | brand |
| `onTertiary` | `#FFFFFF` | `#11270b` Evergreen | derived |

### Neutral ramp (derived — FR-003)

| Role | Light | Dark |
|---|---|---|
| `background` / `surface` | `#FBFDF6` (green-tinted white) | `#0E1A08` (near-Evergreen) |
| `onBackground` / `onSurface` | `#11270b` | `#E3E8DC` |
| `surfaceVariant` | `#E0E5D6` | `#42493B` |
| `onSurfaceVariant` | `#42493B` | `#C2C9B8` |
| `outline` | `#72796A` | `#8C9382` |
| `outlineVariant` | `#C2C9B8` | `#42493B` |
| *disabled fg* | `onSurface` @ 38% alpha | `onSurface` @ 38% alpha |
| *disabled container* | `onSurface` @ 12% alpha | `onSurface` @ 12% alpha |

### Semantic roles (derived — FR-004)

| Role | Light | Dark | Notes |
|---|---|---|---|
| `error` | `#B3261E` | `#F2B8B5` | Material standard |
| `onError` | `#FFFFFF` | `#601410` | |
| `warning`¹ | `#8A5A00` | `#E9B949` | extension color |
| `success`¹ | `#2E7D5B` | `#7FD1AE` | teal-shifted, distinct from brand greens |

¹ `warning`/`success` are not Material `ColorScheme` slots → exposed via a small
`MirabilisColors` extension object accessible through the theme.

**Validation rules**:
- Every role MUST have both a light and dark value (TokenCompletenessTest).
- Every `X` / `onX` pairing and every text-on-surface pairing MUST meet WCAG AA — ≥ 4.5:1 body,
  ≥ 3:1 large text / UI component boundaries (ContrastTest). Values above are seeds; the test is
  the source of truth and tones are tuned until green.

---

## Entity: Typography Style (FR-006)

Roboto-based Material 3 scale. Each style = font family (Roboto) + weight + size (sp) + line
height (sp) + letter spacing.

| Style group | Members |
|---|---|
| Display | `displayLarge`, `displayMedium`, `displaySmall` |
| Headline | `headlineLarge`, `headlineMedium`, `headlineSmall` |
| Title | `titleLarge`, `titleMedium`, `titleSmall` |
| Body | `bodyLarge`, `bodyMedium`, `bodySmall` |
| Label | `labelLarge`, `labelMedium`, `labelSmall` |

**Validation rules**: sizes in `sp` (respect system font scale — FR-016); all 15 roles defined
(TokenCompletenessTest).

---

## Entity: Dimension Token (FR-007)

| Kind | Tokens |
|---|---|
| Spacing | `xs=4dp`, `sm=8dp`, `md=16dp`, `lg=24dp`, `xl=32dp` |
| Corner radius (Shapes) | `small=8dp`, `medium=12dp`, `large=16dp` |
| Elevation | `level0=0dp`, `level1=1dp`, `level2=3dp`, `level3=6dp` |

**Validation rules**: components reference these tokens only — no inline `dp` literals for
spacing/radius/elevation (FR-012).

---

## Entity: Component

A stateless composable defined by its **variants** and **states**, sourcing all appearance from
the tokens above. Full API in [contracts/ui-components.md](contracts/ui-components.md).

| Component | Priority | Variants | States |
|---|---|---|---|
| `MirabilisButton` | P1 | primary, secondary, text/tertiary | enabled, pressed, focused, disabled |
| `MirabilisText` | P1 | any typography role + color role | (static) |
| `MirabilisTextField` | P1 | outlined (default) | empty, focused, filled, error, disabled |
| `MirabilisFab` | P2 | regular, small, extended | enabled, pressed, disabled |
| `MirabilisSwitch` | P2 | — | on, off, disabled |
| `MirabilisCheckbox` | P2 | — | checked, unchecked, disabled |
| `MirabilisRadioButton` | P2 | — | selected, unselected, disabled |
| `MirabilisCard` | P2 | elevated, outlined | (container) |
| `MirabilisChip` | P2 | assist, filter | enabled, selected, disabled |
| `MirabilisTopAppBar` | P2 | small | (container) |
| `MirabilisDialog` | P2 | — | shown |

**Validation rules**:
- Every component renders only from tokens (FR-012).
- Every component has isolated light + dark `@Preview` (FR-019 / SC-007).
- Only defined variants/states are exposed (no runtime "unknown variant" path — edge case).

---

## Entity: Theme

`MirabilisTheme(darkTheme: Boolean = isSystemInDarkTheme(), content)` — a complete mapping of
every color role → concrete value for the active mode, plus typography + shapes, applied via
`MaterialTheme`. Also exposes `MirabilisColors` (warning/success) via a `CompositionLocal`.

**Validation rules**: switching `darkTheme` swaps every role with no missing value (SC-005).
