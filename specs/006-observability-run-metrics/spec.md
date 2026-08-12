# Feature Specification: Per-Commit Run Metrics (Tokens + Run-Health Provenance)

**Feature Branch**: `006-observability-run-metrics`

**Created**: 2026-08-11

**Status**: Draft

**Input**: User description: "Automated per-commit token + run-health observability aggregate for the
AI-native development harness — Phase A of the observability roadmap, implementing ADR-0013 and
ADR-0014 §3 (git-native aggregates only; no traces, logs, or evals). Cost in USD is deferred to a
later phase. Each adapter captures per-session tokens; the ai-paced harness maps a session to one
commit and exports token + run-health values before committing; the commit hook appends them as
trailers; the metrics script aggregates them."

## Overview

This feature makes the AI-native development harness **measure the cost-in-work and health of each
change it produces**, using the same git-native mechanism as existing provenance (ADR-0010). After
an agent produces a change, the commit carries an honest, content-free record of **how many tokens
it took** and **how healthy the run was** (wall-time, retries, errors, outcome). These aggregates
are then summarized per spec / agent / model.

This is **Phase A** of the observability roadmap. It delivers the *metrics* pillar for run cost and
health only.

**Explicitly out of scope (later phases):** monetary cost in USD (needs a price table — deferred),
execution traces, prompt/response logging, quality evals, and any external telemetry sink. No
prompt/response content ever crosses into git or the neutral core.

## Clarifications

### Session 2026-08-11

- Q: Where does the per-model price table come from? → A: **No USD cost in this phase.** Only tokens
  and run-health are recorded; monetary cost is deferred to a later phase (avoids a price table and
  any external dependency now).
- Q: If an adapter cannot capture tokens for a run, what happens? → A: **Omit the trailer** (treated
  as `-` / not-measured; the metrics script skips it). Never fabricate a value; never block the
  commit.
- Q: The outcome enum listed `refusal`/`cutoff`, but those are model-level states the neutral harness
  cannot observe. → A: **This phase records only `ok` / `error` / `cancelled`** (the harness knows
  these). `refusal`/`cutoff` are **deferred to the diagnostic phase (ADR-0014)**, where the adapter
  can detect them; the enum grows there without rework.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Every agent change records tokens + run-health (Priority: P1)

As the maintainer running the ai-paced harness, when an agent produces a commit, that commit should
automatically carry how many tokens the run consumed and how the run went (wall-time, retries,
errors, terminal outcome) — without me writing anything by hand and without any prompt/response
content being stored.

**Why this priority**: This is the core value — turning "how much did this run cost in work / did it
go well?" from an assertion into a git-native fact. Nothing else in this feature matters without it.

**Independent Test**: Run one ai-paced session that produces a commit; inspect the commit's trailers
and confirm token + run-health values are present (or cleanly absent as `-` when unmeasured), with
no prompt/response content anywhere.

**Acceptance Scenarios**:

1. **Given** an ai-paced session that captured token usage, **When** it commits, **Then** the commit
   carries a token trailer and run-health trailers (wall-time, retries, errors, outcome).
2. **Given** a session whose adapter could not capture tokens, **When** it commits, **Then** the
   token trailer is omitted (not `0`), and the commit still succeeds.
3. **Given** any recorded run, **When** the commit is inspected, **Then** no prompt text, response
   text, context, or tool arguments/results appear anywhere in the commit or repository.

---

### User Story 2 - One session maps to one commit (no double counting) (Priority: P1)

As the maintainer, I need each agent session's totals attributed to exactly one commit, so that
summing tokens across history never counts the same work twice.

**Why this priority**: Without a clean session→commit rule the aggregates are wrong (inflated), which
would make the whole metric untrustworthy. It is a correctness prerequisite for US3.

**Independent Test**: Run a session that produces multiple commits; confirm the token/run-health
totals appear on exactly one commit and the others carry `-` for those keys.

**Acceptance Scenarios**:

1. **Given** a session that produces two or more commits, **When** they are created, **Then** the
   session totals appear on exactly one of them and the rest omit those trailers.
2. **Given** the full git history, **When** token totals are summed, **Then** no session's tokens are
   counted more than once.

---

### User Story 3 - Aggregated, honest metrics view (Priority: P2)

As the maintainer, I want the metrics view to summarize tokens and run-health per spec, per agent,
and per model, so I can see where work and failures concentrate — with unmeasured values excluded
and cross-model token sums clearly flagged as approximate.

**Why this priority**: The per-commit record (US1) has value on its own; the aggregation makes it
legible at a glance. It builds on US1/US2.

**Independent Test**: With several commits carrying trailers (some with `-`), run the metrics view
and confirm totals per spec/agent/model exclude `-`, and per-model token sums display the
approximate/"not directly comparable" caveat.

**Acceptance Scenarios**:

1. **Given** commits with token + run-health trailers, **When** the metrics view runs, **Then** it
   reports totals broken down by spec, agent, and model.
2. **Given** commits where some values are `-`, **When** the metrics view runs, **Then** those are
   excluded from sums/averages (not treated as `0`).
3. **Given** token sums spanning different models, **When** they are displayed, **Then** they carry a
   label stating counts are not directly comparable across models (different tokenizers).
4. **Given** the metrics view, **When** it runs, **Then** it makes no network calls and reads only
   from git history.

---

### Edge Cases

- **Local model run**: tokens are recorded if the local runtime reports them, otherwise the trailer
  is omitted (`-`). (Monetary cost is not recorded at all in this phase.)
- **Human (non-agent) commit**: no token/run-health values are exported → trailers omitted; the
  commit is normal.
- **Manually amended commit message**: appending trailers must not duplicate existing ones (idempotent,
  as with existing provenance stamping).
- **Session with zero measured tokens**: only recorded as `0` if genuinely measured as zero;
  otherwise omitted.
- **Session that errors before committing**: no commit, no metric (accepted; there is nothing to
  attribute).

## Requirements *(mandatory)*

### Functional Requirements

#### Capture & attribution

- **FR-001**: The harness MUST capture, per agent session, the total tokens consumed, when the
  adapter is able to report it.
- **FR-002**: The harness MUST capture, per agent session, run-health signals: total wall-time,
  retry count, and terminal outcome (one of: ok, error, cancelled — signals the neutral harness can
  observe). Model-level outcomes (refusal, cutoff) are deferred to the diagnostic phase (ADR-0014).
- **FR-003**: The harness MUST attribute a session's totals to exactly one commit; any additional
  commits from the same session MUST omit those values (no double counting).
- **FR-004**: When a value cannot be measured, the system MUST omit it (treated as not-measured),
  and MUST NOT record a fabricated or zero value in its place.
- **FR-005**: A measured zero MUST be recorded as `0` and remain distinguishable from not-measured.

#### Recording (git-native, content-free)

- **FR-006**: Captured values MUST be recorded as commit metadata (trailers), never in application
  code, and never in a separate tracked file.
- **FR-007**: The system MUST NOT record any prompt text, response text, context, or tool
  arguments/results in git or the neutral core — only numeric/enum aggregates.
- **FR-008**: Trailer stamping MUST be automatic (no manual editing) and idempotent (re-running must
  not duplicate trailers).
- **FR-009**: Monetary cost in USD MUST NOT be recorded in this phase (deferred); the feature MUST
  remain forward-compatible with a later cost field without rework of the recording mechanism.

#### Aggregation & reporting

- **FR-010**: The metrics view MUST aggregate token and run-health values per spec, per agent, and
  per model.
- **FR-011**: The metrics view MUST exclude not-measured values from sums and averages.
- **FR-012**: The metrics view MUST label per-model token sums as approximate / not directly
  comparable across models (different tokenizers).
- **FR-013**: The metrics view MUST be read-only and derive everything from git history, with no
  network calls and no external service.

#### Boundary

- **FR-014**: This feature MUST respect the neutral-core / adapter boundary: raw capture logic lives
  in the tool-specific adapter, while the recording and aggregation mechanism stays tool-neutral;
  no domain/project knowledge is placed in an adapter.

### Key Entities *(include if feature involves data)*

- **Run metric record**: the per-commit set of values — tokens, wall-time, retries, errors, outcome
  — carried as commit trailers. Content-free.
- **Session→commit attribution**: the mapping ensuring one session's totals land on one commit.
- **Aggregated metrics view**: the read-only summary derived from all run metric records, grouped by
  spec / agent / model.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of agent-produced commits in an ai-paced run either carry token + run-health
  trailers or cleanly omit unmeasured ones — with zero hand-editing.
- **SC-002**: Summing tokens across the full git history counts each session at most once (no double
  counting), verifiable on a multi-commit session.
- **SC-003**: Zero prompt/response/context content appears in git or the neutral core for any
  recorded run (verifiable by inspection).
- **SC-004**: The metrics view reports token and run-health breakdowns per spec, agent, and model,
  excluding not-measured values, in under 5 seconds on the current repository and with no network
  access.
- **SC-005**: A not-measured value is never presented as `0` in any commit or in the metrics view.
- **SC-006**: A human (non-agent) commit carries no run-metric trailers and is unaffected.

## Assumptions

- **Cost deferred**: Monetary USD cost is out of scope for this phase; only tokens + run-health are
  recorded. A later phase adds cost via a versioned price table (per ADR-0013), reusing this
  mechanism.
- **Reuse of existing provenance plumbing**: This builds on the existing commit-trailer provenance
  and metrics mechanism (ADR-0010) — the same hook and metrics view are extended, not replaced.
- **Adapter capability varies**: Local and cloud adapters differ in what they can report; missing
  values are represented as not-measured (`-`), not errors.
- **Governed by ADRs**: This feature implements ADR-0013 and ADR-0014 §3 within the boundary set by
  ADR-0012 (adapter-scoped runtime observability) and Principles IV/V.
- **Activation**: Recording is wired into the ai-paced run mode (ADR-0009); human-paced commits
  naturally omit the values.
- **No external sink**: All data stays in git; no tracing backend or telemetry service is introduced
  in this phase.
