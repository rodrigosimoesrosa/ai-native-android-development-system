# `:domain`

The **pure center** — entities, **repository interfaces**, and use cases. No Android/framework
(guarded by `DomainPurityTest`). This is where the DIP arrow points.

- **Depends on:** `:core` only.
- **Rule:** the domain declares abstractions (`IAuthRepository`, `ISessionRepository`); `:data`
  implements them. Use cases are concrete `@Inject` classes; a feature never sees a data model.
- **Decisions:** [ADR-0003](../decisions/ADR-0003-android-architecture-clean-mvi.md),
  [ADR-0004 (DI)](../decisions/ADR-0004-dependency-injection-hilt.md).
