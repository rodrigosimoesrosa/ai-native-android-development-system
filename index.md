# Knowledge Map

Hub note for the project's knowledge graph. The graph is rendered two ways, both from
plain-text markdown in git — no tool lock-in (see [ADR-0001](decisions/ADR-0001-build-on-existing-tools-neutral-core.md)):

- **Mermaid diagram below** — renders inside Android Studio / IntelliJ markdown preview,
  on GitHub, and in Obsidian. No external app needed.
- **Interactive graph** — open this folder as an Obsidian vault (`Open folder as vault`) for
  the force-directed view built from the markdown links.

## Graph

```mermaid
graph TD
    IDX["index.md — Knowledge Map"]
    VIS["Vision & Architecture"]
    CON["Constitution v1.1.0"]
    A1["ADR-0001 · Neutral core + adapters"]
    A2["ADR-0002 · Adopt Spec Kit"]
    A3["ADR-0003 · Clean Architecture + MVI"]
    A4["ADR-0004 · DI with Hilt"]
    A5["ADR-0005 · Persistence: Room + DataStore"]
    A6["ADR-0006 · Networking + auth/refresh"]
    A7["ADR-0007 · Quality gates: detekt + guardrails"]
    A8["ADR-0008 · methods/ + adapters/ layout"]
    A9["ADR-0009 · Run modes: human/ai-paced"]
    A10["ADR-0010 · Automated provenance + metrics"]
    KG["ADR (future) · Knowledge graph"]:::todo
    MET["methods/ — neutral how"]
    ADP["adapters/ — tool invocation"]

    IDX --> VIS
    IDX --> CON
    IDX --> A1
    IDX --> A2
    IDX --> A3
    IDX --> A4
    IDX --> A5
    IDX --> A6
    IDX --> A7
    IDX --> A8
    A7 --> A1
    A7 --> CON
    A8 --> A1
    A8 --> MET
    A8 --> ADP
    IDX --> A9
    A9 --> A8
    A9 --> CON
    A9 --> MET
    IDX --> A10
    A10 --> CON
    A10 --> MET
    A10 --> A9
    A1 --> VIS
    A2 --> A1
    A2 --> VIS
    A2 --> CON
    A3 --> A4
    A3 --> A5
    A3 --> A6
    CON --> VIS
    CON --> A1
    CON --> A2
    CON --> A3
    CON --> A4
    A1 --> MET
    A1 --> ADP
    A1 -.-> KG

    classDef todo stroke-dasharray: 5 5,opacity:0.55;
```

## Foundation

- [Vision & Architecture](docs/00-vision-and-architecture.md) — what this project is and why.
- [Constitution](docs/constitution.md) — the rules humans and agents build by (v1.1.0).

## Decisions (ADRs)

- [ADR-0001 — Neutral core, tools as pluggable adapters](decisions/ADR-0001-build-on-existing-tools-neutral-core.md)
- [ADR-0002 — Adopt GitHub Spec Kit as the SDD engine](decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md)
- [ADR-0003 — Android architecture: Clean Architecture + MVI](decisions/ADR-0003-android-architecture-clean-mvi.md)
- [ADR-0004 — Dependency Injection with Hilt](decisions/ADR-0004-dependency-injection-hilt.md)
- [ADR-0005 — Local persistence: Room + Proto DataStore](decisions/ADR-0005-local-persistence-room-datastore.md)
- [ADR-0006 — Networking + JWT auth/refresh strategy](decisions/ADR-0006-networking-and-auth-token-strategy.md)
- [ADR-0007 — Quality gates: detekt + method guardrails](decisions/ADR-0007-quality-gates-detekt-and-method-guardrails.md)
- [ADR-0008 — Materialize the methods/ + adapters/ layout](decisions/ADR-0008-methods-and-adapters-layout.md)
- [ADR-0009 — Run modes: human-paced / ai-paced](decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md)
- [ADR-0010 — Automated provenance (commit trailers) + metrics](decisions/ADR-0010-automated-provenance-and-metrics.md)

## Specs

One folder per feature (`specs/<NNN-name>/`): spec, plan, research, data-model, contracts, tasks.
Transient work units that travel the loop; the durable decisions they inherit live in the ADRs above.

- [001 — OTP Auth](specs/001-otp-auth/spec.md)
- [002 — User Profile](specs/002-user-profile/spec.md)
- [003 — App Navigation Shell](specs/003-navigation/spec.md)
- [004 — Confirm Sign-Out](specs/004-confirm-sign-out/spec.md) — implemented **ai-paced by opencode** (local LLM)

## Method layer (neutral core vs. adapters)

The portability guarantee of ADR-0001, made concrete (ADR-0008):

- [`methods/`](methods/README.md) — the tool-agnostic "how" of each capability.
- [`adapters/`](adapters/README.md) — the thin, disposable tool layer: [claude-code](adapters/claude-code/README.md) and [opencode](adapters/opencode/README.md) (a **local** LLM) are **both active** — opencode drove spec `004` end-to-end.

## Not created yet (dangling nodes — intentional)

These appear grey in the graph until written:

- ADR (future) — Knowledge-graph representation
