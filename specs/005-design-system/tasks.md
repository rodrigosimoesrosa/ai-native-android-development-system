---

description: "Task list for Design System (005)"
---

# Tasks: Design System (UI Component Source of Truth)

**Input**: Design documents from `/specs/005-design-system/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/ui-components.md](contracts/ui-components.md)

**Governing decisions (ADRs)**: Implement WITHIN these — a task that would conflict is a human
`architecture-change` gate (stop and escalate):
- [ADR-0003](../../decisions/ADR-0003-android-architecture-clean-mvi.md) — Clean+MVI. The design
  system is a **stateless presentation library**: no ViewModels/DI/domain models (see plan.md).
- [ADR-0007](../../decisions/ADR-0007-quality-gates-detekt-and-method-guardrails.md) — detekt +
  method guardrails.
- [ADR-0010](../../decisions/ADR-0010-automated-provenance-and-metrics.md) — provenance trailers.
- [ADR-0011](../../decisions/ADR-0011-visual-regression-testing-deferred.md) — screenshot testing
  **deferred**; use `@Preview` + JVM contrast tests, do NOT add Paparazzi/Roborazzi.

**Tests**: Included — Principle III (NON-NEGOTIABLE) + plan.md require machine-checkable AA
contrast and token completeness. Write test tasks FIRST and ensure they FAIL before implementing.

**Module**: all paths under `core-designsystem/` (new leaf module,
`com.mirabilis.core.designsystem`). No screens are touched (FR-018). No new third-party
dependency — Compose/Material already in `gradle/libs.versions.toml` (no `dependency-add` gate).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependency on incomplete tasks)
- **[Story]**: US1–US4 map to spec.md user stories

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create and wire the new module.

- [x] T001 Register `:core-designsystem` in `settings.gradle.kts` (add `include(":core-designsystem")`)
- [x] T002 Create `core-designsystem/build.gradle.kts` — apply `android.library`, `kotlin.android`, `kotlin.compose`; `namespace = "com.mirabilis.core.designsystem"`; `buildFeatures { compose = true }`; wire ONLY existing catalog deps (compose BOM, `androidx.compose.ui`, `material3`, `ui.tooling.preview`, `debugImplementation(ui.tooling)`) + test deps (`junit`); mirror `feature/auth/build.gradle.kts` (no new catalog entries — see research.md D2)
- [x] T003 [P] Create source package dirs `core-designsystem/src/main/kotlin/com/mirabilis/core/designsystem/{token,theme,component,component/extended,catalog}` and `core-designsystem/src/test/kotlin/com/mirabilis/core/designsystem/`; confirm detekt runs on the module (ADR-0007)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Non-color tokens + theme skeleton that EVERY component and both P1 stories need.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 [P] Create `token/Type.kt` — Roboto-based Material 3 typography scale, all 15 roles (display/headline/title/body/label × L/M/S), sizes in `sp` (FR-006, FR-016; data-model.md)
- [x] T005 [P] Create `token/Shape.kt` — corner-radius tokens (small 8dp / medium 12dp / large 16dp) as a `Shapes` (FR-007)
- [x] T006 [P] Create `token/Spacing.kt` — spacing tokens `xs..xl` (4/8/16/24/32dp) (FR-007)
- [x] T007 [P] Create `token/Elevation.kt` — elevation tokens level0..level3 (FR-007)
- [x] T008 [P] Create `theme/MirabilisExtraColors.kt` — data class (warning/onWarning/success/onSuccess) + `CompositionLocal` for the non-Material slots (data-model.md; contracts §Theme)
- [x] T009 Create `theme/MirabilisTheme.kt` skeleton — `MirabilisTheme(darkTheme, content)` wrapping `MaterialTheme(colorScheme=<param placeholder>, typography, shapes)` and providing extra colors; `MirabilisTheme.extraColors` accessor (depends on T004, T005, T008; contracts §Theme)

**Checkpoint**: Module compiles with typography/shape/spacing/elevation tokens + theme wrapper.

---

## Phase 3: User Story 2 - Full color palette & theming as tokens (Priority: P1) 🎯 MVP foundation

**Goal**: Brand greens + derived neutrals + semantics as named tokens, mapped to roles for light
and dark, driving `MirabilisTheme`. Every component in later phases depends on this.

**Independent Test**: color-swatch preview renders every role in both themes; `./gradlew
:core-designsystem:test` passes ContrastTest + TokenCompletenessTest.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T010 [P] [US2] `test/.../TokenCompletenessTest.kt` — assert every color role has a light AND dark value and all 15 typography roles exist (FR-005, SC-002)
- [x] T011 [P] [US2] `test/.../ContrastTest.kt` — recompute WCAG contrast for every offered fg/bg pairing (X/onX, text-on-surface, semantics) in both themes; assert ≥4.5:1 text / ≥3:1 large & UI (15, SC-003)

### Implementation

- [x] T012 [US2] Create `token/Color.kt` — raw 5 greens + derived 9-step neutral ramp + semantic error/warning/success (internal `Color` constants) per data-model.md (FR-002, FR-003, FR-004)
- [x] T013 [US2] Create `token/ColorRoles.kt` — `lightColorScheme(...)` + `darkColorScheme(...)` role mapping and light/dark `MirabilisExtraColors` from Color.kt (FR-005; research.md D3/D4) (depends on T012)
- [x] T014 [US2] Wire real light/dark schemes + extra colors into `theme/MirabilisTheme.kt`, replacing the T009 placeholder (depends on T013)
- [x] T015 [P] [US2] Create `catalog/ComponentCatalog.kt` with the **token section** — color-swatch + typography `@Preview` (light + dark) showing every role + on-color label (SC-002, FR-017 partial). This is the single gallery file; US4 (T029) extends it with component sections (no separate swatch file — avoids drift)
- [x] T016 [US2] Tune container/on-* tones until ContrastTest + TokenCompletenessTest are GREEN (FR-015, SC-003) (depends on T010, T011, T013)

**Checkpoint**: token layer complete, both themes, all contrast/completeness tests green.

---

## Phase 4: User Story 1 - Consistent, branded core components (Priority: P1) 🎯 MVP

**Goal**: Buttons, text, and text inputs sourced entirely from tokens, in all states.

**Independent Test**: each component's light/dark `@Preview` renders every documented state using
only design-system tokens (no raw hex/dp).

**Depends on**: Phase 3 tokens (US2) + Phase 2 theme.

- [x] T017 [P] [US1] Create `component/MirabilisButton.kt` — Primary/Secondary/Text variants, enabled/pressed/focused/disabled (38%/12% alpha tokens), long-label ellipsis; light + dark `@Preview` (FR-008, FR-012, FR-019; contracts §Buttons)
- [x] T018 [P] [US1] Create `component/MirabilisText.kt` — bound to typography scale + color roles, respects font scale; light + dark `@Preview` (FR-009, FR-016, FR-019; contracts §Text)
- [x] T019 [P] [US1] Create `component/MirabilisTextField.kt` — label/placeholder/helper/error(errorText via `error` role)/filled/focused/disabled; light + dark `@Preview` (FR-010, FR-019; contracts §Text field)
- [x] T020 [P] [US1] (optional) `test/.../ButtonStateTest.kt` — instrumented `ui-test-junit4`: disabled button is not clickable (edge case; catalog dep only)

**Checkpoint**: core components usable and previewable in both themes — MVP design system.

---

## Phase 5: User Story 3 - Extended component catalog (Priority: P2)

**Goal**: The broader common-control set so most screens compose from the system.

**Independent Test**: each extended component's light/dark `@Preview` renders its variants/states
from tokens. Independent of US1 (different files).

**Depends on**: Phase 3 tokens + Phase 2 theme (not on US1).

- [x] T021 [P] [US3] Create `component/MirabilisFab.kt` — Small/Regular/Extended, enabled/pressed/disabled; light + dark `@Preview` (FR-011, FR-019; contracts §FAB)
- [x] T022 [P] [US3] Create `component/extended/MirabilisSwitch.kt` — on/off/disabled + previews (FR-013)
- [x] T023 [P] [US3] Create `component/extended/MirabilisCheckbox.kt` — checked/unchecked/disabled + previews (FR-013)
- [x] T024 [P] [US3] Create `component/extended/MirabilisRadioButton.kt` — selected/unselected/disabled + previews (FR-013)
- [x] T025 [P] [US3] Create `component/extended/MirabilisCard.kt` — elevated/outlined + previews (FR-013)
- [x] T026 [P] [US3] Create `component/extended/MirabilisChip.kt` — assist/filter, enabled/selected/disabled + previews (FR-013)
- [x] T027 [P] [US3] Create `component/extended/MirabilisTopAppBar.kt` — title + nav/actions slots + previews (FR-013)
- [x] T028 [P] [US3] Create `component/extended/MirabilisDialog.kt` — title/text/confirm/dismiss + preview (FR-013)

**Checkpoint**: full component catalog available, each previewable in both themes.

---

## Phase 6: User Story 4 - Discoverable component gallery (Priority: P3)

**Goal**: One surface listing every token + component with variants/states.

**Independent Test**: `ComponentCatalog` light/dark `@Preview` shows all tokens + components;
at max system font scale nothing clips.

**Depends on**: components (US1, US3) + tokens (US2).

- [x] T029 [US4] Extend `catalog/ComponentCatalog.kt` (from T015) with **all component sections** in every state, built ONLY from design-system APIs; update light + dark `@Preview` (FR-017, SC-002, SC-004, SC-006) — same file as T015, so sequential (not [P])

**Checkpoint**: gallery renders the whole system in both themes.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T030 [P] Run `./gradlew :core-designsystem:detekt` and resolve findings (ADR-0007)
- [ ] T031 [P] Verify every component file has BOTH a light and a dark `@Preview` (SC-007) — grep `@Preview` per component file in `core-designsystem/src/main`
- [ ] T032 Run [quickstart.md](quickstart.md) validation: `:core-designsystem:assemble` + `:test` + `:detekt` all green; confirm no raw hex/dp literals inside components (FR-012, SC-001)
- [ ] T033 Update [index.md](../../index.md) Knowledge Map with the design-system module; record provenance trailers on commits (spec 005, ADR-0010, Principle IV)
- [ ] T034 [P] Add KDoc to each component in `core-designsystem/src/main/kotlin/com/mirabilis/core/designsystem/component/**` documenting its available variants and states, per [contracts/ui-components.md](contracts/ui-components.md) (FR-014)

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (P1)** → no deps.
- **Foundational (P2)** → after Setup; BLOCKS all stories.
- **US2 tokens (P3)** → after Foundational; **blocks US1, US3, US4** (they consume tokens).
- **US1 (P4)** and **US3 (P5)** → after US2; independent of each other (can run in parallel).
- **US4 (P6)** → after US1 + US3 (aggregates them); T029 extends the same `ComponentCatalog.kt` created in T015 (US2).
- **Polish (P7)** → after all desired stories.

### Within a story

- Tests (US2: T010–T011) written and FAILING before implementation (T012+).
- Color.kt (T012) → ColorRoles.kt (T013) → wire theme (T014) → tune to green (T016).

### Parallel opportunities

- Setup: T003 [P].
- Foundational: T004–T008 all [P] (T009 waits on T004/T005/T008).
- US2: T010, T011 [P] (tests); T015 [P].
- US1: T017, T018, T019, T020 all [P] (separate files).
- US3: T021–T028 all [P] (separate files).
- After US2 completes, an entire team can take US1 and US3 in parallel.

---

## Parallel Example: after US2 (tokens) is green

```bash
# US1 core components (Developer A):
Task: "Create component/MirabilisButton.kt (T017)"
Task: "Create component/MirabilisText.kt (T018)"
Task: "Create component/MirabilisTextField.kt (T019)"

# US3 extended catalog (Developer B) — in parallel:
Task: "Create component/extended/MirabilisSwitch.kt (T022)"
Task: "Create component/extended/MirabilisCheckbox.kt (T023)"
Task: "Create component/extended/MirabilisCard.kt (T025)"
```

---

## Implementation Strategy

### MVP (branded core)

1. Phase 1 Setup → 2 Foundational → 3 US2 (tokens, tests green) → 4 US1 (core components).
2. **STOP & VALIDATE**: swatch + core-component previews in both themes; `:test` green.
3. This is the demonstrable MVP: a branded, tested, previewable core.

### Incremental delivery

- + US3 (extended catalog) → + US4 (gallery) → Polish.
- Each phase is an independently reviewable, small diff (Principle II). Commit per task/group
  with provenance trailers (ADR-0010).

---

## Notes

- No screen integration (FR-018): `:app`/feature screens stay unchanged in this feature's diff.
- No new dependency; screenshot/visual-regression stays deferred (ADR-0011) — `@Preview` + JVM
  contrast tests are the safeguard.
- Adding the module in `settings.gradle.kts` follows existing modularization; normal human review
  at the merge gate applies.
