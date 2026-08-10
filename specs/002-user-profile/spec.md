# Feature Specification: User Profile

**Feature Branch**: `002-user-profile`

**Created**: 2026-08-10

**Status**: Draft

**Input**: User description: "A dedicated Profile screen for the authenticated user to view and edit
their display name and set a small set of local preferences (theme, notifications). Reuses the
existing session/user and the already-built sign-out from 001-otp-auth (sign-out is NOT in scope).
Excludes the navigation shell (separate 'navigation' feature)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View my profile (Priority: P1)

An authenticated user opens a dedicated Profile screen and sees their own account information
(display name and phone). This is the core value: a home for "my account".

**Why this priority**: Without a place to see the profile, there is nothing to edit or configure.
It is the smallest slice that delivers value on its own.

**Independent Test**: While signed in, open Profile → assert the authenticated user's display name
and phone are shown. Fully testable alone.

**Acceptance Scenarios**:

1. **Given** a signed-in user, **When** they open Profile, **Then** their display name and phone are shown.
2. **Given** the profile cannot be loaded, **When** Profile opens, **Then** a clear, non-sensitive error is shown with a retry.

---

### User Story 2 - Edit my display name (Priority: P2)

A signed-in user changes their display name and it persists, so their account reflects the new name
on subsequent loads.

**Why this priority**: Editing is the first real "manage my account" action; it builds on P1 but P1
is demonstrable without it.

**Independent Test**: Edit the display name to a valid value → save → reopen Profile (or reload) and
assert the new name is shown.

**Acceptance Scenarios**:

1. **Given** the Profile screen, **When** the user submits a valid new display name, **Then** it is persisted and reflected on the next load.
2. **Given** the user submits an empty/blank display name, **When** they try to save, **Then** it is rejected with a clear message and nothing is persisted.
3. **Given** the save cannot complete, **When** the user saves, **Then** a clear, recoverable error is shown and no partial change is persisted.

---

### User Story 3 - Set my preferences (Priority: P3)

A signed-in user toggles a small set of app preferences (theme and notifications) that persist across
app restarts.

**Why this priority**: Preferences are useful but the account view/edit (P1/P2) can be demonstrated first.

**Independent Test**: Toggle a preference → restart the app → assert the preference kept its value.

**Acceptance Scenarios**:

1. **Given** the Profile screen, **When** the user changes a preference, **Then** it is persisted immediately.
2. **Given** a preference was changed, **When** the app is restarted, **Then** the preference retains its value.

### Edge Cases

- **Empty/blank display name** → blocked with a clear message before saving (FR-002).
- **No network during save** → clear, recoverable error; no crash; no partial persistence.
- **Profile opened while not authenticated** → not accessible (FR-005); the user is routed to sign-in.
- **App restarted after changing preferences** → preferences persist (FR-004).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST show the authenticated user's profile information (display name, phone) on a dedicated Profile screen.
- **FR-002**: Users MUST be able to edit their display name; the system MUST reject an empty/blank value with a clear message and persist a valid one.
- **FR-003**: System MUST persist a successful display-name change so it is reflected on subsequent loads.
- **FR-004**: Users MUST be able to set a small set of preferences (at least theme and notifications), persisted locally and retained across app restarts.
- **FR-005**: The Profile screen MUST be accessible only to an authenticated user.
- **FR-006**: On any failure (load or save), the system MUST show a clear, non-sensitive error, allow retry, and never crash or partially persist.
- **FR-007**: Sign-out MUST NOT be provided by this feature — it is owned by the authentication feature (001) and reused where relevant.

### Key Entities *(include if data involved)*

- **User**: the authenticated person's basic profile (identity, phone, display name). Reused from the authentication feature; this feature updates the display name.
- **User Preferences**: a small set of per-user app settings (e.g., theme, notifications) persisted locally.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A signed-in user can view their profile information immediately on opening Profile (100% of the time when a session is valid).
- **SC-002**: A user can change their display name in ≤ 3 primary actions, and the new name is shown on the next load in ≥ 99% of cases.
- **SC-003**: A changed preference is retained across an app restart 100% of the time.
- **SC-004**: Every invalid input (empty name) or failure yields a clear, non-sensitive message and never crashes the app (100%).

## Assumptions

- Reuses the **existing session and `User`** from 001-otp-auth; this feature does not authenticate.
- The **backend is the same mock/fake** as 001; a display-name update follows a mock contract, and a
  real backend can be swapped in later without changing user-facing behavior.
- **Sign-out is out of scope** (owned by 001) and is not duplicated here.
- The **navigation shell** that routes to Profile is a separate feature (`navigation`); this feature
  only exposes a stable entry point (a `profile` route).
- **Single device / single session** (consistent with 001).
- Preferences for v1 are **theme and notifications**; more can be added later.
