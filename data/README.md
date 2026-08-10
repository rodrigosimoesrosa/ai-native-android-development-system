# `:data`

Repository **implementations** + infrastructure (networking, persistence). Maps remote/persistence
models to domain at the boundary; nothing here is domain-visible.

- **Depends on:** `:domain`, `:core`.
- **Networking:** Retrofit/OkHttp, `AuthInterceptor` (bearer) + `TokenAuthenticator`
  (single-flight 401 refresh, anti-loop) — [ADR-0006](../decisions/ADR-0006-networking-and-auth-token-strategy.md).
- **Persistence:** encrypted Proto DataStore for session/user; Room available but unused by
  `001-otp-auth` — [ADR-0005](../decisions/ADR-0005-local-persistence-room-datastore.md).
- **DI:** interface→impl `@Binds` live here — [ADR-0004](../decisions/ADR-0004-dependency-injection-hilt.md).
