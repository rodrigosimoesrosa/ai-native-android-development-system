# `:core`

Pure Kotlin/JVM — **no Android, no framework** (enforced by `CorePurityTest` and the module graph).

- **Owns:** `Result<T>` / `FlowResult<T>` / `AppError` (the typed boundary contract) and the
  `@Dispatcher` coroutine qualifier.
- **Depends on:** nothing (only `javax.inject` + coroutines).
- **Decision:** [ADR-0003 — Clean Architecture + MVI](../decisions/ADR-0003-android-architecture-clean-mvi.md).
