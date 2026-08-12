---

description: "Task list for Per-Commit Run Metrics (006)"
---

# Tasks: Per-Commit Run Metrics (Tokens + Run-Health Provenance)

**Input**: Design documents from `/specs/006-observability-run-metrics/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/)

**Governing decisions (ADRs)**: Implement WITHIN these — a conflicting task is a human
`architecture-change` gate (stop and escalate):
- [ADR-0010](../../decisions/ADR-0010-automated-provenance-and-metrics.md) — provenance trailers +
  metrics (this **extends** its hook + `metrics.sh`).
- [ADR-0013](../../decisions/ADR-0013-per-commit-token-cost-aggregate.md) — token aggregate contract
  (cost deferred this phase).
- [ADR-0014](../../decisions/ADR-0014-llm-diagnostic-telemetry-traces-logs-evals.md) §3 — run-health
  aggregates (latency/retries/errors/outcome).
- [ADR-0012](../../decisions/ADR-0012-llm-runtime-observability-adapter-scoped.md) — adapter-scoped:
  tokens captured in the adapter; only content-free aggregates cross to git.
- [ADR-0009](../../decisions/ADR-0009-run-modes-human-paced-and-ai-paced.md) — run modes;
  [ADR-0007](../../decisions/ADR-0007-quality-gates-detekt-and-method-guardrails.md) — quality gates.

**Tests**: Included — Principle III (NON-NEGOTIABLE) + plan.md. Dependency-free bash tests on temp
git repos (no `bats` — that would trip `dependency-add`). Write test tasks FIRST; ensure they FAIL.

**Scope**: meta-system tooling only (`githooks/`, `scripts/`, `adapters/`). **No app modules.** No new
dependency (git + bash). No prompt/response content ever in git (FR-007). Cost/USD, traces, logs,
evals: out of scope (later phases).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: different files, no dependency on incomplete tasks
- **[Story]**: US1–US3 map to spec.md user stories

---

## Phase 1: Setup

- [x] T001 Create `scripts/tests/` and a dependency-free runner `scripts/tests/run.sh` that discovers and executes `test_*.sh`, each in a throwaway `git init` temp repo, and reports pass/fail (git + bash only)
- [x] T002 [P] Enable the hook for this clone via `scripts/setup-hooks.sh` (sets `core.hooksPath=githooks`) and confirm `githooks/prepare-commit-msg` runs

---

## Phase 2: Foundational (Blocking Prerequisites)

**⚠️ CRITICAL**: blocks all user stories — the shared, neutral sidecar helper is used by the hook,
the harness, and the adapters.

- [x] T003 Create neutral helper `scripts/lib/run-metrics.sh` per [contracts/sidecar-format.md](contracts/sidecar-format.md): `sidecar_path` (`"$(git rev-parse --git-dir)"/mirabilis/run-metrics.env`), `append_metric KEY VALUE` (mkdir -p; accumulate `PROVENANCE_TOKENS`), and a **safe** `read_metrics` (parse `KEY=VALUE` without `source`; honor only known keys; validate int/enum). No tool-specific logic (Principle V)

**Checkpoint**: helper available; sidecar path/format agreed across writers and the hook.

---

## Phase 3: User Story 1 - Every agent change records tokens + run-health (Priority: P1) 🎯 MVP

**Goal**: after a session, the produced commit carries tokens + run-health, content-free, no
hand-editing.

**Independent Test**: write a sidecar (or run a real session), commit, confirm the trailers are
present (or cleanly omitted when unmeasured) and no prompt/response content exists anywhere.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T004 [P] [US1] `scripts/tests/test_hook_trailers.sh` — assert the hook stamps present run-metric keys from sidecar + env, **omits** unset ones (never `0`), preserves a **measured `0`**, emits no content key, and that a commit with **no sidecar and no env carries no run-metric trailers** (human/non-agent commit unaffected) (FR-004/005/007; SC-006; contracts/trailer-schema.md)

### Implementation

- [x] T005 [US1] Extend `githooks/prepare-commit-msg` — via `scripts/lib/run-metrics.sh`, read env then sidecar and stamp present `Provenance-Tokens/Latency-Ms/Retries/Errors/Outcome`, omitting unset keys; validate int/enum before stamping; keep existing merge/squash skip (FR-006/008; depends on T003, T004)
- [x] T006 [P] [US1] Extend `scripts/ai-paced-run.sh` — time the run and, on exit, write run-health to the sidecar via the helper: `PROVENANCE_LATENCY_MS` (wall-time), `PROVENANCE_RETRIES` (fix attempts), `PROVENANCE_ERRORS` (gate failures), `PROVENANCE_OUTCOME` (**`ok`/`error`/`cancelled`** from exit reason — `refusal`/`cutoff` are model-level, deferred to ADR-0014) (FR-002; neutral harness — depends on T003)
- [x] T007 [P] [US1] Extend `adapters/claude-code/run-ai-paced.sh` — parse per-invocation token usage from the claude-code CLI output and accumulate `PROVENANCE_TOKENS` into the sidecar; report `0` only if genuinely measured as zero, otherwise **omit** (FR-001/004/005; ADR-0012 — depends on T003)
- [x] T008 [P] [US1] Extend `adapters/opencode/run-ai-paced.sh` — parse the local model's token counts (e.g. `prompt_eval_count`+`eval_count`) and accumulate `PROVENANCE_TOKENS`; report `0` only if genuinely measured, otherwise **omit** when the runtime does not report them (FR-001/004/005; ADR-0012 — depends on T003)

**Checkpoint**: a real (or simulated) session produces a commit carrying tokens + run-health.

---

## Phase 4: User Story 2 - One session maps to one commit (no double counting) (Priority: P1)

**Goal**: a session's totals land on exactly one commit; summing history never double-counts.

**Independent Test**: a session producing multiple commits → totals on exactly one; second commit
omits the keys; amend does not duplicate trailers.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T009 [P] [US2] `scripts/tests/test_consume_once.sh` — after a stamped commit the sidecar is **deleted**; a second commit carries **no** run-metric trailers; an amend does **not** duplicate trailers (FR-003/008; SC-002)

### Implementation

- [x] T010 [US2] Extend `githooks/prepare-commit-msg` — after stamping, **delete** the sidecar (consume-once) and preserve the existing already-stamped/amend idempotency guard (FR-003/008; same file as T005 → sequential; depends on T005, T009)

**Checkpoint**: recording is correct-once — no double counting.

---

## Phase 5: User Story 3 - Aggregated, honest metrics view (Priority: P2)

**Goal**: `metrics.sh` summarizes tokens + run-health per spec/agent/model, excluding unmeasured,
flagging cross-model token sums.

**Independent Test**: with synthetic commits (some `-`), `metrics.sh` reports breakdowns excluding
`-`, prints the tokenizer caveat, makes no network calls.

### Tests (write FIRST, must FAIL) ⚠️

- [x] T011 [P] [US3] `scripts/tests/test_metrics_agg.sh` — build synthetic stamped commits and assert `metrics.sh` sums tokens/latency/retries/errors and breaks down outcome per spec/agent/model, **excludes `-`** (not `0`), prints the **tokenizer caveat**, emits **no `Provenance-Cost`** (deferred, FR-009), and issues **no network calls** (FR-010/011/012/013; SC-004/005; contracts/metrics-cli.md)

### Implementation

- [x] T012 [US3] Extend `scripts/metrics.sh` — aggregate `Provenance-Tokens/Latency-Ms/Retries/Errors` and `Provenance-Outcome` per spec/agent/model in both `text` and `--markdown`, skipping `-`, honoring total-wins, labeling per-model token sums "not directly comparable across models (different tokenizers)"; stay read-only/git-only/<5s (FR-010–013; depends on T011)

**Checkpoint**: legible, honest aggregate view.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T013 [P] Update [methods/record-provenance.md](../../methods/record-provenance.md) documenting the new run-metric keys, the sidecar channel, and the `0`-vs-`-` semantics (ADR-0013/0014)
- [x] T014 [P] Add an optional read-only `scripts-tests` job to `.github/workflows/ci.yml` running `bash scripts/tests/run.sh`
- [x] T015 Run [quickstart.md](quickstart.md) end-to-end: `scripts/tests/run.sh` green, manual smoke stamps + consumes the sidecar, `check-adapter-boundary.sh` + `check-knowledge.sh` green (FR-014)
- [x] T016 Confirm `scripts/metrics.sh` renders the new breakdowns on real history; record provenance trailers on commits (spec 006, ADR-0010)

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (P1)** → no deps.
- **Foundational (P2, T003)** → after Setup; **blocks US1/US2/US3**.
- **US1 (P3)** → after T003; delivers stamping + writers.
- **US2 (P4)** → after US1 (T010 extends the same hook file as T005); delivers consume-once.
- **US3 (P5)** → after T003; independent of US1/US2 (tests use synthetic commits).
- **Polish (P6)** → after the stories it documents/validates.

### Within a story

- Tests first and FAILING before implementation (T004→T005; T009→T010; T011→T012).
- T005 (hook stamp) before T010 (hook delete) — same file, sequential.

### Parallel opportunities

- Setup: T002 [P].
- US1: T006, T007, T008 all [P] (harness + two adapters — different files); T004 [P] (test).
- US2 / US3 tests: T009, T011 [P].
- Polish: T013, T014 [P].
- After T003, the harness writer (T006) and both adapter writers (T007/T008) can be done by different
  people in parallel; US3 (metrics) can proceed alongside US1/US2 since it tests on synthetic commits.

---

## Parallel Example: after Foundational (T003)

```bash
# US1 writers, in parallel (different files):
Task: "Extend scripts/ai-paced-run.sh — write run-health to sidecar (T006)"
Task: "Extend adapters/claude-code/run-ai-paced.sh — accumulate tokens (T007)"
Task: "Extend adapters/opencode/run-ai-paced.sh — accumulate tokens (T008)"

# US3 metrics track, in parallel with US1/US2:
Task: "Write test_metrics_agg.sh (T011)"
Task: "Extend scripts/metrics.sh aggregation (T012)"
```

---

## Implementation Strategy

### MVP (correct-once recording)

1. Setup → Foundational (T003) → US1 (stamping + writers) → US2 (consume-once).
2. **STOP & VALIDATE**: a session's commit carries tokens + run-health exactly once; tests green.
3. This is the demonstrable MVP: honest, git-native run metrics per commit.

### Incremental delivery

- + US3 (aggregate view) → Polish (docs, CI job, quickstart). Each phase is a small, reviewable diff
  (Principle II), committed with provenance trailers (ADR-0010).

---

## Notes

- No new dependency; tests are plain bash on temp repos.
- Tokens live only in `adapters/*` (Principle V); run-health + mechanism stay neutral — keep
  `check-adapter-boundary.sh` green.
- Content-free: only numeric/enum trailers; no prompt/response/context ever reaches git (FR-007).
- Known limitation (research D2): an unrelated human commit made before the produced change would
  consume the sidecar — accepted for Phase A.
