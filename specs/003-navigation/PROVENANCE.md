# Provenance — `003-navigation`

Per Constitution Principle IV (*Knowledge in Git, with Provenance*): who/what produced this feature,
recorded outside the app code. Machine ledger = `Provenance-*` commit trailers (ADR-0010).

## What

An authenticated **navigation shell** (bottom nav) — new `:feature:navigation` module — that hosts the
app's top-level destinations (Home from 001, Profile route from 002), lets a signed-in user switch
between them while preserving each destination's state, and is shown only when authenticated.

## How (method)

Executed in **ai-paced** run mode (ADR-0009): an agent worked the approved `tasks.md` (T001–T013) to
completion via the SDD loop (`methods/sdd-loop.md`) — implement → verify → mark done — passing the
verification gate (`methods/verify-change.md`) before each phase. No specify/plan step (that was done
and approved in human-paced first). Human gates (merge, dependency-add, architecture-change, release)
were NOT crossed.

## Who (agent)

- **Agent:** agent (Claude Code)
- **Model:** `claude-sonnet-5`
- **Human gates:** the maintainer owns the merge gate and reviews the change request. See escalations below.

## Key decisions & corrections recorded

- **Module dependency direction (avoid a cycle).** `tasks.md`/`plan.md` place `NavShell` inside
  `:feature:navigation` (which hosts Home from `:feature:auth`) *and* wire it into `AuthRoot` (in
  `:feature:auth`). Taken literally that is a circular module dependency. Resolved with **slot
  inversion**: `AuthRoot`/`AuthNavGraph` gained an `authenticatedContent` slot (default = host Home
  directly, so `:feature:auth` stays standalone), and `:app`/`MainActivity` supplies `NavShell`.
  Dependency runs one way: `navigation → auth`; `app → {auth, navigation}`. Within the recorded
  architecture (ADR-0003) — no architecture-change gate crossed.
- **002 seam / graceful degradation (FR-006).** This branch is cut from `main`; `:feature:profile`
  does not exist. `:feature:navigation` does **not** depend on it. `PROFILE_ROUTE_AVAILABLE = false`
  disables the Profile tab and shows a placeholder; Home stays usable; no crash. At the merge gate with
  002 the flag flips and the placeholder is replaced by the real Profile screen at the shared `profile`
  route id — the single cross-feature interface.
- **Icons kept dependency-free.** `Destination.icon` is an emoji glyph rendered via `Text`, avoiding a
  `material-icons` artifact — honors the "boring tech / no new deps" constitution check.
- **Test scaffolding (dependency-add — flagged for the merge gate).** To run the mandated instrumented
  test (T007), test-only entries were added to the version catalog: `androidx.compose.ui:ui-test-junit4`
  and `:ui-test-manifest` (versioned by the already-pinned Compose BOM) and `androidx.test.ext:junit`
  `1.2.1` (the AndroidJUnitRunner the app already declares). No functional/runtime third-party deps
  added. Navigation Compose was already present. The reviewer owns final approval of these test deps.

## Verification

- **JVM slice (green):** `:feature:navigation:testDebugUnitTest` (`DestinationsTest`, `NavGatingTest`) +
  `:feature:auth:testDebugUnitTest` (001 unchanged, still green) + `:app:assembleDebug` (proves the
  module graph builds, no cycle) + `detekt` (all modules clean).
- **Instrumented (green):** `:feature:navigation:connectedDebugAndroidTest` — `NavShellStateTest`
  (switch, no-reload-on-reselect, nested state kept across switch). **3 tests PASSED on emulator-5554
  (Small_Phone AVD-15).** Targeted with `ANDROID_SERIAL=emulator-5554` (a physical device was also
  attached and rejected installs with `INSTALL_FAILED_USER_RESTRICTED`).
