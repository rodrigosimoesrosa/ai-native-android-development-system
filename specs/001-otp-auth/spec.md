# Feature Specification: OTP Phone Authentication with Session Continuity

**Feature Branch**: `001-otp-auth`

**Created**: 2026-08-07

**Status**: Draft

**Input**: User description: "OTP phone authentication: user submits phone to request an OTP, verifies the received code to obtain access + refresh credentials, session persisted securely; access auto-refreshed on 401; screens SendPhone, VerifyPhone, Home"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sign in with a phone number via one-time code (Priority: P1)

A new or returning user opens the app, enters their phone number, receives a one-time code
(out of band), enters the code, and lands on the authenticated Home screen showing their
account. This is the core value: passwordless sign-in.

**Why this priority**: Without it there is no authentication and no product. It is the MVP —
the smallest slice that delivers value on its own.

**Independent Test**: Enter a valid phone → request code → enter the correct code → assert the
user reaches Home authenticated and their account information is shown. Fully testable alone.

**Acceptance Scenarios**:

1. **Given** the SendPhone screen, **When** the user submits a valid phone number, **Then** the system requests a code and advances to the VerifyPhone screen for that phone.
2. **Given** the VerifyPhone screen for a pending phone, **When** the user submits the correct code, **Then** an authenticated session is established and the user is taken to Home.
3. **Given** the user reached Home, **When** Home loads, **Then** the authenticated user's information is displayed.
4. **Given** the VerifyPhone screen, **When** the user submits an incorrect or expired code, **Then** a clear, non-sensitive error is shown and the user may retry.

---

### User Story 2 - Stay signed in without re-entering a code (Priority: P2)

A signed-in user closes and reopens the app, and keeps using protected features over time,
without being asked to sign in again while their session remains valid — even when the
short-lived access credential expires between actions.

**Why this priority**: Session continuity is what makes the auth usable day to day; it builds
directly on P1 but P1 is demonstrable without it.

**Independent Test**: With a valid persisted session, reopen the app and assert Home loads
without a code prompt; force the access credential to expire, trigger a protected action, and
assert it completes with no visible re-authentication.

**Acceptance Scenarios**:

1. **Given** a previously authenticated user, **When** the app is restarted, **Then** the user lands on Home without entering a code.
2. **Given** a valid session whose access credential has expired, **When** the user performs a protected action, **Then** the system renews access transparently and the action succeeds with no visible interruption.
3. **Given** several protected actions occurring at the exact moment the access credential expires, **When** they are processed, **Then** access is renewed only once and all actions proceed.

---

### User Story 3 - Graceful session end (Priority: P3)

When a user signs out, or when the session can no longer be renewed, the user is returned to
the sign-in screen and can no longer see protected data.

**Why this priority**: Correct teardown protects the user and closes the loop, but the happy
path (P1/P2) can be demonstrated first.

**Independent Test**: Trigger sign-out (or simulate a non-renewable session) and assert the
user is returned to SendPhone and protected data is no longer accessible.

**Acceptance Scenarios**:

1. **Given** an authenticated user on Home, **When** they sign out, **Then** the session is cleared and they are returned to the SendPhone screen.
2. **Given** an authenticated user, **When** the session can no longer be renewed, **Then** the session is ended, stored credentials are cleared, and the user is returned to SendPhone.
3. **Given** a session was ended, **When** the app is reopened, **Then** the user must sign in again to reach Home.

### Edge Cases

- **Invalid phone format** on SendPhone → blocked before requesting a code, with a clear message.
- **Wrong code** → clear, non-sensitive error; retry allowed until the backend rate-limits (FR-014).
- **Expired code** → error explaining the code expired; user can request a new one.
- **Resend** requested too soon → blocked by a cooldown with a clear message.
- **No network** during request/verify/protected call → clear, recoverable error; no crash.
- **Renewal fails** (session no longer valid) → session ends, user returned to sign-in.
- **App killed mid-flow** during verification → the pending verification is not retained; the user restarts from SendPhone.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST let a user request a one-time verification code by submitting a phone number.
- **FR-002**: System MUST validate the phone number format before requesting a code and reject malformed input with a clear message.
- **FR-003**: System MUST let a user verify their phone by submitting the received code for the pending request, and on success establish an authenticated session.
- **FR-004**: System MUST reject an incorrect or expired code with a clear, non-sensitive error and allow the user to retry.
- **FR-005**: Users MUST be able to request a new code (resend) when the previous one did not arrive or expired, subject to a cooldown.
- **FR-006**: System MUST persist the authenticated session so the user remains signed in across app restarts.
- **FR-007**: System MUST store session credentials securely at rest — never readable in plaintext.
- **FR-008**: System MUST keep the user's access valid transparently while the session is valid, renewing an expired short-lived access credential without requiring the user to re-verify.
- **FR-009**: When multiple protected actions occur while the access credential is expired, the system MUST renew access only once and then let all actions proceed (no duplicate renewals).
- **FR-010**: When the session can no longer be renewed, the system MUST end the session, clear stored credentials, and return the user to the sign-in screen.
- **FR-011**: Users MUST be able to sign out, which clears the session and returns them to the sign-in screen.
- **FR-012**: Protected screens (Home) MUST be inaccessible without a valid session and MUST show the authenticated user's information.
- **FR-013**: The pending phone-verification challenge MUST be transient — never persisted beyond the active sign-in flow.
- **FR-014**: System MUST gracefully handle backend rate-limiting / lockout responses (e.g., "too many attempts"): show a clear, non-sensitive message and prevent further attempts until the backend permits them again. The thresholds (attempt count, cooldown duration) are **enforced by the backend**, not defined by the app.

### Key Entities *(include if feature involves data)*

- **Phone Verification Challenge**: a pending request to verify a specific phone number; holds an opaque verification handle and an expiry; strictly transient (exists only during the sign-in flow).
- **Authenticated Session**: represents the user's signed-in state; conceptually a short-lived access credential plus a longer-lived renewal credential; persisted securely; can be renewed or ended.
- **User**: the authenticated person; basic identity/profile displayed on Home.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time user can go from phone entry to an authenticated Home in ≤ 3 primary actions and under 60 seconds, excluding out-of-band code-delivery latency.
- **SC-002**: A returning user with a valid session reaches Home without entering a code in ≥ 99% of app launches.
- **SC-003**: When the access credential expires during use, the user's action completes with no visible re-authentication in ≥ 99% of cases.
- **SC-004**: Concurrent protected actions during credential expiry cause zero duplicate renewals (exactly one renewal) in automated tests.
- **SC-005**: When the session cannot be renewed, the user is returned to sign-in and can no longer access protected data, 100% of the time.
- **SC-006**: Every invalid phone or code input yields a clear, non-sensitive error and never crashes the app (100%).

## Assumptions

- The backend is a **mock/fake** with defined contracts for this feature; a real backend can be swapped in later without changing user-facing behavior.
- The **code-delivery channel** (SMS or email) is out of scope — the app only requests and verifies; delivery is the backend's responsibility.
- The one-time code is a **numeric code** (assumed 6 digits). The **backend owns** code format, expiry, resend cooldown, and attempt/lockout thresholds — the app reflects the backend's responses and does not define these limits (see FR-014).
- **Single device / single session**; multi-device session management is out of scope for v1.
- Phone numbers are validated in **international format** (E.164-like) by default.
- **Home** shows the current user's basic profile obtained from a protected endpoint (this protected call is what exercises transparent renewal).
- **Network connectivity is required** to sign in; offline sign-in is out of scope.
