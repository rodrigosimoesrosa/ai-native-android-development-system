# ADR-0005: Local persistence — Room (relational) + Proto DataStore (typed state)

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [ADR-0003 (architecture)](ADR-0003-android-architecture-clean-mvi.md), [ADR-0004 (DI/Hilt)](ADR-0004-dependency-injection-hilt.md), [constitution](../docs/constitution.md)
- **Adapted from:** `plato-app-android` (Room 2.6.1 + Proto DataStore 1.0.0). We simplify Plato's
  double local mapping (`Room @Entity → OrmEntity.toData() → XxxData`) into a single mapping.

---

## Context

Persistence is a cross-cutting concern that must be decided **before** the first feature, not
rediscovered at task time. It has to fit ADR-0003's rules: persistence lives entirely inside
`:data`, the domain and UI never see a storage type, and everything crosses boundaries as
typed `Result`/`FlowResult`. We also want an **offline-first** posture where the local store is
the single source of truth.

Two distinct storage needs exist and must not be forced into one tool:
- **Relational / queryable data** (lists, relationships, many rows, paging) — e.g. **`Product`**,
  orders, messages → **Room**.
- **Small typed state / single object** (the current authenticated **`User`**, session token,
  settings, feature flags, onboarding-done, last-sync) → **Proto DataStore**.

> **Rule of thumb:** *"many rows I query"* → Room; *"one object / a handful of typed values"* →
> DataStore. The authenticated `User` is a **single object**, so it lives in DataStore, **not** a
> Room table. Room examples in this repo use `Product` (relational); `User` is the DataStore example.

## Decision

Use **Room** for relational data and **Proto DataStore** for small typed state. They are
complementary, not competing (the Google-recommended split; also what Plato does).

### Room — relational feature data (e.g. `Product`)
- **`ProductEntity` is a Room-only persistence type**, confined to a `data/database` package inside
  `:data` — Room annotations live *only* here. Map **directly** `ProductEntity ↔ Product` in the
  Repository (read: `ProductEntity.toDomain()`, write: `Product.toEntity()`) — one hop, **no separate
  local model**. The rest of `:data` (repositories, remote datasource) never imports a Room type,
  so swapping Room touches only `ProductEntity` + its mappers. *(Gets Plato's `OrmEntity` isolation
  without its redundant `Entity → XxxData → domain` double hop.)*
- **DAO** interfaces in `:data`; wrapped by an `ILocalDataSource` (interface) whose impl extends
  `SafeLocalDataSource` — DB exceptions become `AppError.Database`, never a crash.
- **Offline-first / single source of truth:** the Repository exposes `Flow<FlowResult<Product>>`
  backed by Room (a `Flow`-returning DAO query). The remote datasource only *writes into* Room;
  Room re-emits, the UI updates. This pairs naturally with `FlowResult` (Loading/Success/Error).
- Use `room-paging` when a screen needs paging; `room-testing` (in-memory DB) for fast, real
  TDD of DAOs and repositories.

### Proto DataStore — single-object / typed state (e.g. the current `User`, settings, flags)
- **Same model-per-source discipline as Room.** A `.proto` schema **generates** a typed message
  (e.g. `UserProto`), confined to a `data/datastore` package. A `UserLocalDataSource` /
  `SettingsLocalDataSource` wraps `DataStore<UserProto>` (with a `Serializer<UserProto>`), exposes
  `Flow<User>`, and maps **directly** `UserProto ↔ User` (read: `toDomain()`, write: `toProto()`).
  Errors → `AppError.DataStore`. Domain never sees `UserProto`.
- **Naming:** `UserProto` (generated) is the DataStore analogue of `ProductEntity` (Room) — the
  persistence type stays confined to `:data` and is mapped straight to domain in one hop. So the
  persistence-type suffix signals the engine: `XxxEntity` = Room, `XxxProto` = DataStore.
- **Offline-first:** DataStore is the source of truth for the current user; sign-in writes into it,
  the UI observes `Flow<User>`.
- Chosen over **Preferences DataStore** for type-safety + schema (a better reference showcase);
  the cost is the protobuf plugin + codegen. Not Room, because this data is a **single object**, not relational.

### Where instances are created (per ADR-0004)
Entities, DAOs, `.proto`, and datasource impls live in `:data`. The `RoomDatabase` and
`DataStore` **instances** (which need `Context`) are provided by Hilt modules in `:app` (the
composition root). Domain stays pure; `:data` exposes only interfaces.

### Versions — pinned in `gradle/libs.versions.toml`
- **Room `2.8.4`** (latest stable, verified on Google Maven 2026-08-07) — `room-runtime`,
  `room-ktx`, `room-paging`, `room-testing`; compiler via **KSP**.
- **DataStore `1.2.1`** (latest **stable** — deliberately **not** `1.3.0-alpha10`; constitution
  requires boring, deterministic deps).
- **Protobuf** runtime + Gradle plugin for Proto DataStore — pin current stable at setup
  (Plato baseline: protobuf `3.25.0`, plugin `0.9.1`; confirm compatibility with the chosen
  protobuf major at skeleton time).

## Consequences

### Positive
- Domain and UI never depend on Room/DataStore — persistence is swappable inside `:data` (DIP).
- Right tool per job; offline-first single-source-of-truth pairs cleanly with `FlowResult`.
- In-memory Room + test DataStore make persistence trivially unit-testable (TDD).
- Typed errors (`AppError.Database`, `AppError.DataStore`) end-to-end.

### Negative / costs
- Two storage mechanisms to learn and wire.
- Proto DataStore adds protobuf codegen setup vs. the simpler Preferences DataStore.

### Neutral
- Simpler local mapping than Plato (one hop, not two) — an intentional divergence.

## Alternatives considered

1. **SQLDelight** instead of Room. Rejected: less mainstream in a pure-Android stack, and not in
   the maintainer's current toolbelt; Room is the boring/standard choice (constitution).
2. **Preferences DataStore** instead of Proto. Rejected as the default: untyped/stringly keys;
   Proto is a stronger typed showcase. (Preferences remains acceptable for trivial one-off flags.)
3. **SharedPreferences** for key-value. Rejected: legacy, synchronous, no Flow.
4. **One store for everything** (Room-only or DataStore-only). Rejected: forces the wrong tool
   onto one of the two data shapes.

## Resulting actions

- [ ] Pin Room `2.8.4` + DataStore `1.2.1` + protobuf in `libs.versions.toml` (KSP for Room).
- [ ] `:data/database`: `ProductEntity` `@Entity` + DAO (Room confined here) + `SafeLocalDataSource`.
- [ ] `:data/datastore`: `user.proto` → `UserProto` + `Serializer` + `UserLocalDataSource`; settings/flags datasources.
- [ ] `:app`: Hilt modules providing `RoomDatabase` and `DataStore` instances.
- [ ] First feature proves the offline-first read path (Room as source of truth) + a write path.
