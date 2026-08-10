# Feature Specification: App Navigation Shell

**Feature Branch**: `003-navigation`

**Created**: 2026-08-10

**Status**: Draft

**Input**: User description: "An authenticated navigation shell (e.g. bottom navigation) that hosts
the app's top-level destinations — starting with Home and Profile — letting the user move between
them while preserving each destination's state. It hosts existing screens (Home from 001, Profile
from the 'profile' feature) rather than rebuilding them, and depends on Profile only for a route id."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Move between top-level sections (Priority: P1)

A signed-in user switches between the app's main sections (Home and Profile) using a persistent
navigation control, without going back through the sign-in flow. This is the core value: the app
becomes navigable beyond a single linear flow.

**Why this priority**: Without a way to move between sections, additional screens (like Profile) are
unreachable. It is the smallest slice that makes the app a multi-section app.

**Independent Test**: While signed in, use the navigation control to switch from Home to Profile and
back → assert each target section is shown. Testable with placeholder destinations.

**Acceptance Scenarios**:

1. **Given** a signed-in user on the shell, **When** they select another destination, **Then** that destination is shown and the control reflects the active section.
2. **Given** the user is on a destination, **When** they select the current destination again, **Then** they stay there (no disruptive reload).

---

### User Story 2 - Keep each section's state when switching (Priority: P2)

When a signed-in user switches away from a section and returns, that section is where they left it
(its own back stack / scroll / input state), rather than reset.

**Why this priority**: State preservation is what makes multi-section navigation feel usable; it
builds on P1 but P1 is demonstrable without it.

**Independent Test**: Navigate into a nested state on one section, switch to another, switch back →
assert the first section retained its state.

**Acceptance Scenarios**:

1. **Given** a section with in-section state, **When** the user switches away and back, **Then** the section retains its previous state.
2. **Given** several switches, **When** the user returns to a section, **Then** its back stack is intact.

---

### User Story 3 - Authenticated-only, sensible start (Priority: P3)

The navigation shell is shown only to an authenticated user and opens on a sensible default section
(Home). An unauthenticated user never reaches the shell.

**Why this priority**: Correct gating and a predictable entry protect the flow, but P1/P2 can be
demonstrated first.

**Independent Test**: Launch signed in → assert the shell opens on the default section; simulate no
session → assert the shell is not shown and the user is routed to sign-in.

**Acceptance Scenarios**:

1. **Given** a signed-in user, **When** the app opens, **Then** the shell is shown on the default destination (Home).
2. **Given** no valid session, **When** the app opens, **Then** the shell is not shown and the user is routed to sign-in.

### Edge Cases

- **Unauthenticated access** → the shell is not shown; the user is routed to sign-in (FR-004).
- **A destination's route is unavailable** (e.g. Profile not yet present) → the shell degrades gracefully; no crash (FR-006).
- **Rapid switching** between destinations → no lost or duplicated state; the active section stays correct.
- **Returning after session ends** (sign-out/teardown from 001) → the shell is dismissed and the user returns to sign-in.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an authenticated navigation container exposing the app's top-level destinations (at least Home and Profile).
- **FR-002**: Users MUST be able to switch between destinations from a persistent navigation control that indicates the active destination.
- **FR-003**: The system MUST preserve each destination's own state / back stack when the user switches away and returns.
- **FR-004**: The navigation shell MUST be shown only to an authenticated user; an unauthenticated user MUST be routed to sign-in and never reach the shell.
- **FR-005**: The shell MUST host existing destinations without reimplementing them — Home is owned by the authentication feature (001) and Profile by the `profile` feature.
- **FR-006**: If a destination's route is unavailable, the shell MUST degrade gracefully (no crash) and keep the other destinations usable.
- **FR-007**: When the session ends (sign-out or a non-renewable session), the shell MUST be dismissed and the user returned to sign-in.

### Key Entities *(include if data involved)*

- **Destination**: a top-level section reachable from the shell — a stable route identifier plus a display label/icon. Home and Profile are the initial destinations. (No new persisted data.)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A signed-in user can switch between Home and Profile from a persistent control in a single action, 100% of the time.
- **SC-002**: Returning to a section preserves its state in ≥ 99% of switches.
- **SC-003**: The shell is shown only when authenticated and never to an unauthenticated user (100%).
- **SC-004**: An unavailable destination route never crashes the app (100%); the other destinations remain usable.

## Assumptions

- Composes with the **existing top-level auth-state routing** from 001-otp-auth (the shell is the
  authenticated area; sign-in remains outside it).
- **Home already exists** (001) and is hosted, not rebuilt.
- **Profile is a separate feature** (`002-user-profile`); the shell depends only on a stable `profile`
  **route id**, not its implementation — this is the single cross-feature interface, resolved at merge.
- **Bottom navigation** is the assumed presentation for v1 (a drawer is an acceptable alternative);
  the exact control is a design detail, not a requirement.
- **Single device / single session** (consistent with 001).
