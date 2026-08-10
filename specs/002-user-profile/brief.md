# Brief: User Profile

**Feature**: profile · **Status**: Draft · **Author**: maintainer

## Problem / context
After signing in (001-otp-auth) the user only *sees* basic identity on Home. There is no dedicated
place to review and **edit** their profile (display name) or set app **preferences**. This adds a
Profile screen where the authenticated user manages their own information.

## Goals
- A Profile screen showing the authenticated user's info (from the existing session/`User`).
- Let the user **edit** their display name and persist it.
- Let the user set a small set of **preferences** (e.g. theme, notifications) persisted locally.

## Non-goals (out of scope)
- **Sign out** — already implemented in 001-otp-auth (reuse it; do not rebuild).
- Authentication, session, or token handling — owned by 001-otp-auth.
- The navigation shell that routes to this screen — that is the `navigation` feature.
- Server-side profile management beyond the existing mock contract.

## Users / stakeholders
The authenticated end user.

## Constraints & assumptions
- Follows the architecture baseline (ADR-0003/04/05/06): Clean + MVI, pure domain, typed `Result`.
- Preferences persist via Proto DataStore (ADR-0005); a display-name edit updates the `User` (mock backend).
- Reuses the existing `User` domain model and session from 001.

## Success signals (outcome)
- A signed-in user updates their display name and sees it reflected on the next load.
- Preferences survive an app restart.

## Open questions
- [ ] Which preferences are in scope for v1 (theme? notifications?).
- [ ] Does the display-name edit call a protected endpoint (mock) or stay local until synced?

## Scope boundary (for multi-agent disjointness)
- **New:** `:feature:profile` (screen + ViewModel), `:domain` profile/preferences use cases,
  `:data` profile update + preferences datastore.
- **Reuses (no edits expected):** `:domain` `User`/session interfaces, `:core`.
- **Interface with `navigation`:** exposes a stable route id `profile` — the only shared point.
