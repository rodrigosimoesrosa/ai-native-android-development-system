# Quickstart & Validation: App Navigation Shell

Built on 001 (+ ideally 002 for the real Profile destination). Details in
[data-model.md](./data-model.md) and [research.md](./research.md).

## Run
```bash
./gradlew test                 # JVM unit tests (destinations, auth-gating decision)
./gradlew :app:installDebug    # then sign in → the shell hosts Home + Profile
```
Sign in (reuses 001) → land on the shell (Home) → switch to Profile via the bottom nav.

## Scenarios → acceptance / success criteria

| # | Scenario | Proves | Kind |
|---|---|---|---|
| 1 | Signed in → switch Home ↔ Profile from the bottom nav | AC1.1, FR-001/002, SC-001 | instrumented (nav) |
| 2 | Re-selecting the current tab does not disruptively reload | AC1.2 | instrumented |
| 3 | Nested state on a tab → switch away & back → state kept | AC2.1/2.2, FR-003, SC-002 | instrumented |
| 4 | Launch signed in → shell opens on default (Home) | AC3.1, FR-004 | JVM (gating) + instrumented |
| 5 | No valid session → shell not shown, routed to sign-in | AC3.2, FR-004, SC-003 | JVM (gating via `AuthRoot`) |
| 6 | Profile route absent → Profile tab disabled/placeholder, Home usable, no crash | Edge, FR-006, SC-004 | JVM/instrumented |
| 7 | Session ends (sign-out/teardown, 001) → shell dismissed → sign-in | Edge, FR-007 | reuse 001 routing |

## Key checks
- **Reuse**: Home (001) and Profile (002) are **hosted**, not rebuilt; auth gating reuses `ObserveAuthState` (001).
- **Disjointness**: the only seam with 002 is the `profile` route id (resolved at merge).
- JVM-testable slice = destinations list + the Authenticated→shell decision; nav behavior = instrumented.
