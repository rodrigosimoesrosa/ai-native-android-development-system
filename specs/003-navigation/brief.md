# Brief: App Navigation Shell

**Feature**: navigation · **Status**: Draft · **Author**: maintainer

## Problem / context
Post-login (001-otp-auth) the app is a linear flow (SendPhone → VerifyPhone → Home) with no way to
move between top-level sections. This adds an authenticated **navigation shell** (e.g. bottom
navigation) that hosts the app's main destinations — starting with **Home** and **Profile**.

## Goals
- An authenticated navigation container with top-level destinations.
- Register **Home** (existing) and **Profile** (the `profile` feature) as destinations.
- Preserve each destination's own back stack / state as the user switches.

## Non-goals (out of scope)
- The **Home** screen itself — already exists in 001-otp-auth (host it, do not rebuild).
- The **Profile** screen content — owned by the `profile` feature (host its route, do not build it).
- Auth flow / sign-in routing — owned by 001-otp-auth.

## Users / stakeholders
The authenticated end user.

## Constraints & assumptions
- Follows the architecture baseline (ADR-0003…): Compose Navigation, MVI where stateful.
- Shown only when authenticated (composes with the existing top-level auth-state routing / `AuthRoot`).
- Depends on `profile` only for a **route id**, not its implementation.

## Success signals (outcome)
- A signed-in user switches between Home and Profile from a persistent nav control.
- Switching preserves state; the correct start destination shows on launch.

## Open questions
- [ ] Bottom navigation vs navigation drawer for v1.
- [ ] Where the shell sits relative to the existing `AuthRoot` / auth-state routing.

## Scope boundary (for multi-agent disjointness)
- **New:** `:feature:navigation` (the shell) + navigation wiring in `:app`.
- **Reuses (no edits expected):** `:feature:auth` Home destination, `:core-ui`.
- **Interface with `profile`:** consumes the `profile` route id — the only shared point.
