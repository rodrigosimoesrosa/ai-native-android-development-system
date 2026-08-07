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
    A6["ADR-0006 · Knowledge graph"]:::todo
    MET["methods/"]:::todo
    ADP["adapters/"]:::todo

    IDX --> VIS
    IDX --> CON
    IDX --> A1
    IDX --> A2
    IDX --> A3
    IDX --> A4
    IDX --> A5
    A1 --> VIS
    A2 --> A1
    A2 --> VIS
    A2 --> CON
    A3 --> A4
    A3 --> A5
    CON --> VIS
    CON --> A1
    CON --> A2
    CON --> A3
    CON --> A4
    A1 -.-> A6
    A1 -.-> MET
    A1 -.-> ADP

    classDef todo stroke-dasharray: 5 5,opacity:0.55;
```

## Foundation

- [Vision & Architecture](docs/00-vision-and-architecture.md) — what this project is and why.
- [Constitution](docs/constitution.md) — the rules humans and agents build by (v1.0.0).

## Decisions (ADRs)

- [ADR-0001 — Neutral core, tools as pluggable adapters](decisions/ADR-0001-build-on-existing-tools-neutral-core.md)
- [ADR-0002 — Adopt GitHub Spec Kit as the SDD engine](decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md)
- [ADR-0003 — Android architecture: Clean Architecture + MVI](decisions/ADR-0003-android-architecture-clean-mvi.md)
- [ADR-0004 — Dependency Injection with Hilt](decisions/ADR-0004-dependency-injection-hilt.md)
- [ADR-0005 — Local persistence: Room + Proto DataStore](decisions/ADR-0005-local-persistence-room-datastore.md)

## Not created yet (dangling nodes — intentional)

These appear grey in the graph until written:

- ADR-0006 — Knowledge-graph representation
- `methods/` — the neutral "how" behind each skill
- `adapters/` — tool-specific invocation layer
