# Phase 0 Research: Per-Commit Run Metrics

**Feature**: 006-observability-run-metrics | **Date**: 2026-08-11

No open `NEEDS CLARIFICATION` remained after the spec's clarifications (cost deferred; unmeasured ⇒
omit). The decisions below resolve the "how".

---

## D1 — The capture→commit bridge: a `.git`-local session sidecar

**Problem**: The agent brain and `scripts/ai-paced-run.sh` **do not commit** (the brain is told
"Do not commit"; the harness opens a change request on green). The actual commit happens later,
often by a human in a **different shell** — so `PROVENANCE_*` environment variables set by the
adapter cannot reach commit time the way agent/model attribution nominally does.

**Decision**: Introduce a transient **session sidecar** file at
`"$(git rev-parse --git-dir)"/mirabilis/run-metrics.env` — inside `.git/`, so it is **never tracked,
never in the working tree**, and survives across shells within the same clone. Format: plain
`KEY=VALUE` lines using the `PROVENANCE_*` names (see [contracts/sidecar-format.md](contracts/sidecar-format.md)).

- Writers append to it: the **harness** (run-health) and the **adapter** (tokens).
- The **`prepare-commit-msg` hook reads it, stamps present keys, then deletes it** — *consume-once*.

**Rationale**: Decouples capture (session process) from commit (later/human) without changing *who*
commits or *when*. Reuses the existing env-var convention as the value source; the sidecar is just a
durable, cross-shell carrier the hook already-naturally consumes.

**Alternatives considered**:
- *Env vars only* — rejected: lost when the commit happens in another shell/after the run.
- *Harness owns the commit* — rejected for Phase A: changes the run-mode contract (ADR-0009) and the
  "human reviews/commits at the merge gate" flow; larger blast radius.
- *A tracked file (e.g. `metrics.jsonl`)* — rejected: second source of truth, drift (Principle IV).

---

## D2 — Session → one commit (no double counting) falls out of consume-once

**Decision**: Because the hook **deletes** the sidecar after stamping the first commit, only that
commit carries the session's metrics; any later commit finds no sidecar and omits the keys. This
directly implements FR-003 / US2 with no extra bookkeeping.

**Known limitation (accepted, documented)**: if a human makes an *unrelated* commit before the
change the session produced, that commit would consume the sidecar. Acceptable for Phase A; the
normal ai-paced flow is "run → review → commit the produced change". Recorded as an assumption.

**Alternatives considered**: tagging the sidecar with the branch/spec and only consuming on a
matching commit — deferred as unnecessary complexity for Phase A.

---

## D3 — Capture split along the neutral/adapter boundary (Principle V)

**Decision**:
- **Run-health = neutral harness.** `scripts/ai-paced-run.sh` already tracks fix/total iterations and
  gate pass/fail and can time itself. It writes `PROVENANCE_LATENCY_MS` (wall-time), `PROVENANCE_RETRIES`
  (fix attempts), `PROVENANCE_ERRORS` (gate failures), `PROVENANCE_OUTCOME` (`ok`/`error`/`cancelled`,
  mapped from exit reason) to the sidecar on exit. This is tool-neutral process data. **Model-level
  outcomes (`refusal`/`cutoff`) are out of scope here** — only the adapter can observe them, so they
  are deferred to the diagnostic phase (ADR-0014); the enum grows there without reworking this
  mechanism.
- **Tokens = tool adapter.** Only `adapters/<tool>/run-ai-paced.sh` can read its model's usage, so it
  accumulates `PROVENANCE_TOKENS` into the sidecar.

**Rationale**: Keeps the mechanism and process signals in the neutral core while the only
tool-specific bit (token usage) stays in the adapter — `check-adapter-boundary.sh` stays green.

---

## D4 — Adapter token capture: feasibility & the omit rule

**Decision**: Best-effort per adapter; **omit if unavailable** (FR-004), never fabricate.
- **claude-code**: the CLI can emit usage (input/output tokens) in structured print output; the
  adapter parses per brain-invocation and accumulates into the sidecar.
- **opencode** (local): read the model runtime's token counts (e.g. `prompt_eval_count` +
  `eval_count`) when exposed; if the local runtime does not report them, omit `PROVENANCE_TOKENS`.

**Rationale**: Honors "measure, don't assert" — a `-`/omitted token is honest; a `0` would lie
(ADR-0013 §2). Cross-model token totals are only approximate (different tokenizers) — surfaced as a
label in metrics, not normalized (D6).

**Alternatives considered**: block the run when tokens are unavailable — rejected (turns
observability into a fragile gate); record `0` — rejected (conflates free/zero with not-measured).

---

## D5 — Cost deferred, but forward-compatible

**Decision**: Emit **no** `Provenance-Cost` in this phase. The sidecar/hook/metrics mechanism is
key-agnostic, so a later phase adds `Provenance-Cost` (with a versioned price table, ADR-0013) by
adding one key — no rework. `Provenance-Tokens` is the cross-adapter yardstick meanwhile.

---

## D6 — metrics.sh aggregation

**Decision**: Extend `scripts/metrics.sh` (already reads `Provenance-*` via
`trailers:key=…,valueonly`, already skips blank/`-`) to also total `Provenance-Tokens`,
`Provenance-Latency-Ms`, `Provenance-Retries`, `Provenance-Errors` and break down `Provenance-Outcome`,
per spec / agent / model. Per-model token sums are printed with an explicit **"not directly
comparable across models (different tokenizers)"** label (FR-012). Stays read-only, git-only,
< 5s (FR-013/SC-004). The existing CI `metrics` job runs it unchanged.

---

## D7 — Testing without a new dependency (Principle III)

**Decision**: Add dependency-free **bash test scripts** under `scripts/tests/`, run by
`scripts/tests/run.sh`, each spinning up a throwaway `git init` temp repo to assert:
- hook stamps present keys, **omits** unset ones, is **idempotent** (no dupes on amend), and
  **consumes** (deletes) the sidecar → one-commit-per-session;
- a measured `0` is preserved and distinct from omitted;
- `metrics.sh` excludes `-`, sums per spec/agent/model, and prints the tokenizer caveat.

**Rationale**: `bats` or similar would trip the `dependency-add` human gate; plain bash on temp repos
needs only git. An optional `scripts-tests` CI job runs the suite.

**Alternatives considered**: bats-core — rejected (new dependency). Manual verification only —
rejected (violates Principle III).
