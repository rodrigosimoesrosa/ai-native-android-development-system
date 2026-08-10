# `:core-ui`

Android library holding the generic **MVI base** — `MVIViewModel`, `Reducer`, and the
`UiState/UiEvent/UiEffect/UiIntent` marker interfaces.

- **Flow:** `Intent → onIntent → Event → Reducer → State`; one-shot `Effect` via a buffered channel.
- **Ported from** `mvi-sample/` with ADR-0003 fixes (`setEvent`/`setEffect` protected; `History` removed).
- **Decision:** [ADR-0003](../decisions/ADR-0003-android-architecture-clean-mvi.md).
