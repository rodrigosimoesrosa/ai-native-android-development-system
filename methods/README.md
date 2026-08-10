# methods/ — the neutral "how"

The **tool-agnostic** description of each capability this project relies on, in prose. A *method*
says **what a capability does and how**, independent of any AI tool. The intelligence lives here; a
[tool adapter](../adapters/) is only a thin layer that *invokes* a method (ADR-0001, ADR-0008).

**Rule:** a method names **no tool** (no product names, no command syntax, no file mechanisms). If you
find yourself writing "run `/x`" or a tool's file name, that belongs in `adapters/<tool>/`, not here.

## Capabilities

- [`sdd-loop.md`](sdd-loop.md) — the one repeatable loop: specify → plan → break down → implement →
  verify → record.
- [`run-modes.md`](run-modes.md) — running that loop human-paced or ai-paced (one axis, not a fork).
- [`write-adr.md`](write-adr.md) — record a decision as a versioned, linkable artifact.
- [`verify-change.md`](verify-change.md) — the feedback loop that decides when a change is "done".
- [`record-provenance.md`](record-provenance.md) — trace every change to its spec, method, and author.

Each method is realized per tool under [`adapters/`](../adapters/). To onboard: read the method for
the *what*, then your tool's adapter for the *how here*.
