# ADR-0003: Android architecture — pragmatic Clean Architecture + MVI

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [ADR-0001](ADR-0001-build-on-existing-tools-neutral-core.md), [Vision & Architecture](../docs/00-vision-and-architecture.md), [constitution](../docs/constitution.md), [ADR-0004 (DI with Hilt)](ADR-0004-dependency-injection-hilt.md)
- **Adapted from:** the maintainer's `plato-app-android` (error/Result model, datasource pattern) and `mvi-sample/` (MVI base). This ADR **corrects** Plato's inverted dependency direction (see Alternatives).

---

## Context

The proving-ground Android app needs one architecture that every feature follows, decided
**before** any feature spec or task (an architecture is a cross-cutting decision, not a
feature spec). It must make each change's blast radius small, keep the domain trivially
unit-testable (TDD, constitution Principle III), and express SOLID concretely.

Two existing references were reviewed:
- **`plato-app-android`** — mature Result/error model, `Safe*DataSource` wrappers, repository
  coordination. **But** its Repository interfaces and `Result`/`BaseError` live in `data`, so
  `domain` depends on `data` (arrow `domain → data`) — an inversion of the Clean Architecture
  Dependency Rule. Mapping `Data → Domain` also happens in the UseCase, so the Repository
  leaks data models.
- **`mvi-sample/`** — a clean generic MVI base (`Intent → Event → Reducer → State`, plus
  one-shot `Effect`). Extracted from Plato's `core/mvi`.

Because this repository is a **Clean Architecture reference for a Staff/Principal portfolio**,
replicating Plato's inverted arrows would undercut the thesis. We keep Plato's excellent
patterns and fix the direction.

## Decision

Adopt a **pragmatic Clean Architecture** (clear layers, no dogmatic layer-per-everything)
with **MVI** in the presentation layer. Models are per-layer and mapped at every boundary.

### Module structure (Gradle modules — compiler enforces the dependency rule)

```
:core        (pure Kotlin/JVM)   Result<T>, FlowResult<T>, AppError, dispatchers
:core-ui     (Android lib)       MVI base (MVIViewModel, Reducer, UiState/Event/Effect/Intent)
:domain      (pure Kotlin/JVM)   entities, repository INTERFACES, use cases     → depends on :core
:data        (Android lib)       repository IMPLS, datasources, remote/local models, mappers
                                                                                 → depends on :domain, :core
:feature:<x> (Android lib)       MVI ViewModels, Compose UI, UI state models     → depends on :domain, :core, :core-ui
:app         (Android app)       composition root — Hilt wiring (see ADR-0004)   → depends on all
```

**Dependency rule (the crux — DIP):** `:data → :domain → :core`. The domain is the pure
center and **never** references data, Android, or any framework. A feature never sees a data
model. The compiler enforces this because the modules simply don't depend the wrong way.

### Model per layer + mapping at boundaries

For a relational, Room-backed aggregate `Product` (see [ADR-0005](ADR-0005-local-persistence-room-datastore.md)
for why `Product` is the Room example and `User` is a DataStore example):

| Model | Module / layer | Purpose |
|---|---|---|
| `ProductRemote` | `:data` (remote) | Matches the **API contract**; used only by the Remote DataSource |
| `ProductEntity` | `:data` / `database` pkg | Matches the **Room table**; Room annotations confined here, mapped straight to `Product` |
| `Product` | `:domain` | The **domain** model; the only product type UseCases and features ever see |

- **Read-path:** `API → ProductRemote → [map] → Product` (or `Room → ProductEntity → [map] → Product`).
  The **Repository implementation** performs the mapping and returns **domain** `Product`. UseCases
  are pure domain — they never touch `ProductRemote`/`ProductEntity`. *(This is what you described,
  and it is the textbook-correct location — an improvement over Plato, which maps in the UseCase.)*
- **Write-path (inverse mappers):** when saving/updating, the domain object flows outward:
  `Product → [map] → ProductRemote` (to send to the endpoint) or `Product → [map] → ProductEntity`
  (to persist in Room). The Repository owns these inverse mappers too.
- Mappers are plain extension functions colocated in `:data` (e.g., `ProductRemote.toDomain()`,
  `Product.toEntity()`). No mapper is domain-visible.
- **Persistence type name signals the engine:** `XxxEntity` = Room; `XxxProto` = Proto DataStore
  (single-object/state aggregates like `User` — see ADR-0005). Both are confined to `:data` and
  mapped straight to domain.

### Communication contract (adapted from Plato)

Every layer boundary speaks in **Result**, never raw exceptions:

- **`:core` owns the contracts** (so both `:domain` and `:data` share them without coupling):
  - `Result<T>` — sealed `Success(data)` | `Error(AppError)`. For one-shot `suspend` calls.
  - `FlowResult<T>` — sealed `Success` | `Error` | `Loading`. For reactive streams (adds `Loading`).
  - `AppError` — sealed: `Network`, `Server(status, body)`, `Database`, `EmptyData`, `Unknown(throwable)`. Typed, not stringly.
  - Helpers: `fold(onError, onSuccess)`, `Flow<T>.asResult()`, `Flow<FlowResult<A>>.map { }`.
- **DataSources** extend a `SafeRemoteDataSource` / `SafeLocalDataSource` base that wraps the
  IO call in `try/catch` and converts any throwable into a typed `AppError` → returns
  `Result<XxxRemote/Local>`. A DataSource never leaks a raw exception.
- **Repository** coordinates local+remote, maps to domain, returns `Result<Product>` /
  `Flow<FlowResult<Product>>`.
- **UseCase** applies business rules over domain `Result<Product>` — no mapping, no data types.
- **Presentation (MVI)** folds `Result` into UI `State`/`Effect`.

*(Simplification over Plato: one `Result` + `FlowResult`; we drop Plato's extra `RemoteResult`
type — the `Safe*DataSource` returns `Result` directly. Fewer concepts, same guarantees.)*

### Presentation — MVI (from `mvi-sample`, with fixes)

Generic base `MVIViewModel<STATE, EVENT, EFFECT, INTENT>` with a `Reducer`:
`Intent` (user action, from UI) → business logic → `Event` (internal) → `Reducer.reduce(old, event)` → new `State`; `Effect` for one-shots (navigation, snackbars). Each feature defines its own
immutable `UiState` (loading/error/data + UI-only flags).

**UI model (`ProductUi`) is the exception, not the rule.** By default the `UiState` holds the
**domain** model (`Product`) directly — the UI consumes domain, no extra mapper. Introduce a
per-feature UI model **only** when there is real presentation logic: formatting (price, dates,
i18n), derived/combined fields, UI-only flags (`isSelected`, `isExpanded`), or aggregating
several domain models into one view object. Adding a UI model by default is the over-abstraction
the vision (§0) warns against.

Fixes applied vs. the sample:
- `setIntent` stays **public** (UI input); `setEvent` and `setEffect` become **`protected`**
  (only the ViewModel produces events/effects — closes the unidirectional-flow leak).
- `History` (state time-travel) is **removed** until it's a real, tested feature (no dead code).

### How SOLID shows up (constitution Principle II)

- **SRP:** DataSource = IO only; Repository = coordination + mapping; UseCase = one business rule.
- **OCP:** `Result`/`AppError` are sealed — add cases without editing consumers' branches (exhaustive `when`).
- **LSP:** interfaces for datasources and repositories; impls are substitutable (fakes in tests).
- **ISP:** small, aggregate-scoped interfaces (`IUserRepository`, not a god-repository).
- **DIP:** **domain defines the repository interfaces; data implements them** — this is exactly
  what makes the dependency arrow point inward. It is the single most important rule here.

### Why this makes TDD (Principle III) cheap
`:domain` is pure Kotlin with zero framework deps → use cases and entities are unit-tested with
plain JUnit, no Robolectric/Android. Repositories are tested against fake datasources; ViewModels
against fake use cases. Test-first is frictionless precisely because the arrows point inward.

## Consequences

### Positive
- Domain is pure and trivially testable; features never see data models.
- Compiler-enforced boundaries → small, legible blast radius per change (constitution Principle II).
- Typed errors end-to-end; no raw exceptions crossing layers.
- Concrete, reviewable SOLID — good portfolio signal.

### Negative / costs
- More modules and more mapping boilerplate than Plato's approach (the price of a pure domain).
  Mitigated by keeping Clean *pragmatic* (no ceremony for trivial cases) and colocating mappers.
- Two result types (`Result` + `FlowResult`) to learn.

### Neutral
- Diverges from the maintainer's Plato codebase on dependency direction and mapping location —
  intentional and documented here.

## Alternatives considered

1. **Replicate Plato as-is** (repo interfaces + `Result` in `data`, `domain → data`, mapping in
   UseCase). Rejected: inverts the Dependency Rule; weakens a Clean Architecture reference.
2. **MVVM instead of MVI.** Rejected: MVI's single immutable state + explicit intents/effects
   gives stronger unidirectional guarantees and easier state testing — a better teaching example.
3. **Dogmatic Clean** (entity/model/mapper per micro-concept everywhere). Rejected: the vision
   (§0) flags over-abstraction as a risk; we stay pragmatic.

## Resulting actions

- [ ] Create module skeleton: `:core`, `:core-ui`, `:domain`, `:data`, `:feature:*`, `:app`.
- [ ] Implement `:core` contracts: `Result`, `FlowResult`, `AppError`, dispatchers.
- [ ] Port the MVI base into `:core-ui` with the two fixes above.
- [ ] The first `/speckit-specify` feature exercises the full read+write path end-to-end (TDD).
- [ ] DI wiring per [ADR-0004](ADR-0004-dependency-injection-hilt.md).
