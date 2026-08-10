# `:app`

The **composition root** — the only module that knows the full DI graph.

- **Depends on:** all modules.
- **Hosts:** `@HiltAndroidApp` application (primes the token cache at cold start), `MainActivity`
  (Compose `NavHost`), and the infrastructure Hilt modules (`NetworkModule`, `DataStoreModule`,
  `DispatchersModule`).
- **Decisions:** [ADR-0004 (DI)](../decisions/ADR-0004-dependency-injection-hilt.md),
  [ADR-0006 (networking)](../decisions/ADR-0006-networking-and-auth-token-strategy.md).
