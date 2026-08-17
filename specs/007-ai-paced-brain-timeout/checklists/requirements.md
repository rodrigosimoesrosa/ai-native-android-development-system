# Specification Quality Checklist: ai-paced brain-call liveness timeout

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-08-12
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Notes

- Motivated by a concrete production incident: the ai-paced run of spec 005 hung ~9h on a single
  brain call (opencode → local model) and required a manual kill.
- Core design decision — **measure progress, not duration** — resolves the cloud-vs-local latency
  tension via a progress-based idle-timeout (primary) plus an absolute hard-cap (backstop), with
  per-adapter thresholds so the neutral core stays brain-agnostic (ADR-0001/0008).
- One item flagged for human review: whether adding `Provenance-Outcome: timeout` is a minor
  extension of ADR-0014 or warrants its own ADR (architecture-change gate) — not self-approved.
