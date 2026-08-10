# Constitution (pointer)

The **canonical** project constitution is generated and owned by Spec Kit and lives at
`.specify/memory/constitution.md`. That path starts with a dot, so graph tools like Obsidian
do not index it. This note is a thin, graph-visible **pointer** to it — it holds no governance
content of its own (single source of truth stays in the canonical file), so nothing here can
drift out of sync.

- **Read the real thing:** open `.specify/memory/constitution.md` (current: **v1.1.0**).
- **Why it lives there:** Spec Kit's `/speckit-*` skills read the constitution from that fixed
  location. This is the architecture working as designed — the constitution's *content* is core,
  but the *location* is dictated by the tool adapter (see
  [ADR-0002](../decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md)).

## In the knowledge graph

This node connects the constitution to the rest of the graph:

- [Vision & Architecture](00-vision-and-architecture.md) — where the principles come from.
- [ADR-0001 — Neutral core, tools as pluggable adapters](../decisions/ADR-0001-build-on-existing-tools-neutral-core.md)
- [ADR-0002 — Adopt GitHub Spec Kit](../decisions/ADR-0002-adopt-spec-kit-as-sdd-engine.md)
