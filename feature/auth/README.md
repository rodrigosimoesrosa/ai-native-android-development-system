# `:feature:auth`

The OTP auth feature — MVI ViewModels + Compose screens for **SendPhone → VerifyPhone → Home**,
plus top-level auth-state routing (`AuthRoot`).

- **Depends on:** `:domain`, `:core`, `:core-ui`.
- **Transient challenge:** held in memory only (`PendingVerificationStore`) — never persisted (FR-013).
- **Spec:** [`specs/001-otp-auth/`](../../specs/001-otp-auth/).
- **Decisions:** [ADR-0003](../../decisions/ADR-0003-android-architecture-clean-mvi.md),
  [ADR-0004](../../decisions/ADR-0004-dependency-injection-hilt.md).
