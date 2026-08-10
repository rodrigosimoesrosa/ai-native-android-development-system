# Phase 1 Data Model: App Navigation Shell

This feature has **no persisted data** and no domain entities of its own — it is presentation/
navigation. The only model is a UI descriptor.

## UI descriptor (`:feature:navigation`)

### Destination
| Field | Type | Notes |
|---|---|---|
| `route` | `String` | stable route id (e.g. `home`, `profile`) |
| `label` | `String` | tab label |
| `icon` | (UI) | tab icon |

Initial destinations: **Home** (`home`, from 001) and **Profile** (`profile`, from 002). The list is a
static, ordered set; no repository, no persistence.

## Interfaces / reuse

- **Consumes:** `:feature:auth` Home composable/route; `:feature:profile` `profile` route id (the
  single cross-feature seam with 002).
- **Auth gating:** reuses the existing `ObserveAuthStateUseCase` (001) via `AuthRoot` — the shell is
  shown only when Authenticated; it does not re-implement session logic.

## State (nav)

```
AuthRoot: Authenticated → NavShell(start=home)
NavShell: select destination → switch, preserving each destination's saved state (FR-003)
session ends (001/US3) → AuthRoot routes to sign-in → shell dismissed (FR-007)
profile route absent → Profile tab disabled/placeholder, Home usable (FR-006)
```

No `data-model` persistence tasks — nothing crosses a storage boundary here.
