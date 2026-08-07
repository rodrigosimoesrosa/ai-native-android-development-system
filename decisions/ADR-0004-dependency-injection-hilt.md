# ADR-0004: Dependency Injection with Hilt

- **Status:** Accepted
- **Date:** 2026-08-07
- **Deciders:** Project maintainer
- **Related:** [ADR-0003 (architecture)](ADR-0003-android-architecture-clean-mvi.md), [constitution](../docs/constitution.md)
- **Adapted from:** `plato-app-android` DI layout (Hilt modules per layer, `@InstallIn(SingletonComponent)`).

---

## Context

ADR-0003 fixed the dependency arrows (`:data → :domain → :core`, domain pure). We need a DI
mechanism that wires interfaces to implementations at a single composition root **without**
re-coupling the pure domain to Android. The maintainer's Plato app already uses Hilt/Dagger
with per-layer modules; we adopt it and adjust one thing so `:domain` stays pure JVM.

**Hilt is not an alternative to Dagger — it is built on Dagger 2.** So "Dagger + Hilt" is not
two competing tools; it is one stack used at two levels: Hilt's Android sugar
(`@HiltAndroidApp`, `@HiltViewModel`, `@InstallIn`) on the Android modules, and plain Dagger /
`javax.inject` (constructor injection, `@Qualifier`) on the pure JVM modules where Hilt's
Android components don't reach. Plato already declares both artifact families (`dagger` +
`dagger-compiler` **and** `hilt-android` + `hilt-android-compiler`) — we do the same.

## Decision

Use **Hilt** (Dagger under the hood). The rules:

### 1. Keep `:domain` and `:core` pure — inject with `javax.inject` only
`:domain` and `:core` are pure Kotlin/JVM. They use **only `javax.inject`** (`@Inject`,
`@Qualifier`, `@Named`) — which is plain Java with no Android/Hilt dependency. They contain
**no `@Module` / `@InstallIn`** (those require Hilt's Android components).

- Use cases and repository *implementations* are concrete classes with an `@Inject constructor`.
  Hilt constructs them automatically — **no module needed** for the common case.
- Prefer concrete injectable use cases over interfaces (ISP, pragmatic). Introduce a use-case
  interface only when a real second implementation or test-double boundary demands it.

### 2. Interface→impl bindings live where the impl lives (Android modules)
- **Repositories:** the binding `IUserRepository → UserRepository` goes in a Hilt `@Module` in
  **`:data`** (both types visible there; `:data` is an Android library).
- **Infrastructure** (Retrofit/network, Room/database, DataSources, dispatchers): Hilt modules
  in **`:app`** (the composition root) or `:data`, `@InstallIn(SingletonComponent::class)`.
- **Dispatchers:** a `@Qualifier` (`@Dispatcher(IO|Default)`) declared in `:core` (pure, allowed),
  with the providing `@Module` in an Android module. This lets `Safe*DataSource` receive the IO
  dispatcher by qualifier (as Plato does).

### 3. `:app` is the composition root
`:app` is the only module that knows the full graph. It hosts the `@HiltAndroidApp` application
and the top-level infrastructure modules. Feature ViewModels are `@HiltViewModel` with
`@Inject constructor` depending on domain use cases.

### 4. Versions — pinned in a Gradle version catalog (`libs.versions.toml`)
- **Dagger + Hilt core: `2.60.1`** (latest on Maven Central, verified 2026-08-07). One version
  for `com.google.dagger:dagger`, `dagger-compiler`, `hilt-android`, `hilt-android-compiler`,
  and the `com.google.dagger.hilt.android` Gradle plugin — they share the Dagger version number.
- **androidx.hilt extensions** (`hilt-navigation-compose`, `hilt-compiler` for `@HiltViewModel`
  in Compose/nav) version **independently** — pin the current stable in the catalog at setup
  (Plato baseline: `hilt-navigation-compose 1.2.0`). Confirm latest when the skeleton is created.
- **KSP** (not kapt) for annotation processing — matched to the project's Kotlin version.
- All versions live in `gradle/libs.versions.toml`; no version literals in module `build.gradle.kts`
  (constitution: determinism & reproducibility).

### Module map (mirrors Plato, corrected for a pure domain)

```
:core        @Qualifier Dispatcher (pure, no @Module)
:data        RepositoriesModule  (@Binds/@Provides IUserRepository -> UserRepository)
             DataSourcesModule   (local/remote datasource bindings)
:app         NetworkModule, DatabaseModule, DispatchersModule, ...  (@InstallIn Singleton)
             @HiltAndroidApp Application
:feature:x   @HiltViewModel ViewModels (@Inject constructor(useCases...))
```

## Consequences

### Positive
- Single composition root; domain stays framework-free and unit-testable (TDD).
- Compile-time DI graph validation (Dagger) — misuse fails the build, not runtime.
- Matches the maintainer's existing Hilt experience/patterns.

### Negative / costs
- Hilt adds annotation processing (KSP) build cost.
- The "pure domain" rule means use-case interface bindings (when used) can't live in `:domain`
  — they go to `:app`/`:feature`. A minor, documented indirection.

### Neutral
- Hilt is Android-specific; per [ADR-0001](ADR-0001-build-on-existing-tools-neutral-core.md) that is fine — it is app infrastructure, not the neutral knowledge core.

## Alternatives considered

1. **Koin.** Rejected: runtime resolution (errors at runtime, not compile time); the maintainer
   already knows Hilt; Hilt is the Android-standard for this stack (constitution: boring tech).
2. **Manual DI / no framework.** Rejected: viable for a tiny app but doesn't scale as a
   reference; Hilt better demonstrates a production wiring.
3. **Hilt modules inside `:domain`.** Rejected: would make the domain depend on Hilt's Android
   components, breaking the pure-domain rule from ADR-0003.

## Resulting actions

- [ ] Pin Dagger/Hilt `2.60.1` + KSP in `gradle/libs.versions.toml`; add `@HiltAndroidApp` in `:app`.
- [ ] `RepositoriesModule` in `:data`; infrastructure modules in `:app`.
- [ ] `@Dispatcher` qualifier in `:core`; dispatcher `@Module` in `:app`.
- [ ] First feature's ViewModel is `@HiltViewModel` with injected use cases.
