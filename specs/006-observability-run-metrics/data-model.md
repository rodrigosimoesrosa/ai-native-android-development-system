# Phase 1 Data Model: Per-Commit Run Metrics

**Feature**: 006-observability-run-metrics | **Date**: 2026-08-11

No database. "Data" = the trailer schema (durable), the session sidecar (transient), and the derived
aggregate view. Entities map to spec.md → Key Entities.

---

## Entity: Run metric record (commit trailers)

Content-free values carried as trailers on the commit that a session produced. Extends the ADR-0010
`Provenance-*` block.

| Trailer key | Type | Meaning | Source |
|---|---|---|---|
| `Provenance-Tokens` | integer | total tokens the session's model calls consumed | adapter |
| `Provenance-Latency-Ms` | integer | session wall-time in ms | harness |
| `Provenance-Retries` | integer | per-task fix attempts spent in the session | harness |
| `Provenance-Errors` | integer | gate/tool failures during the session | harness |
| `Provenance-Outcome` | enum | `ok` \| `error` \| `cancelled` (harness-observable; `refusal`/`cutoff` deferred to ADR-0014) | harness |

Also present (unchanged, ADR-0010): `Provenance-Spec`, `-Method`, `-Agent`, `-Model`. **Not** in this
phase: `Provenance-Cost` (deferred, D5).

**Validation rules**:
- A key is present only if **measured**; otherwise **omitted** (= not-measured). Never fabricated.
- A measured zero is `0` and stays distinct from omitted (FR-004/005).
- No key may contain prompt/response/context/tool content (FR-007) — all are numeric/enum.
- Integers ≥ 0; `Provenance-Outcome` ∈ the enum above.

---

## Entity: Session sidecar (transient, `.git`-local)

The capture→commit bridge (research D1). Not tracked, not in the working tree.

- **Path**: `"$(git rev-parse --git-dir)"/mirabilis/run-metrics.env`
- **Format**: `KEY=VALUE` lines using `PROVENANCE_*` names, e.g.:
  ```
  PROVENANCE_TOKENS=48213
  PROVENANCE_LATENCY_MS=42150
  PROVENANCE_RETRIES=2
  PROVENANCE_ERRORS=0
  PROVENANCE_OUTCOME=ok
  ```
- **Lifecycle** (state transitions):
  1. *absent* → harness/adapter **append** keys as the session runs/ends → *populated*
  2. *populated* → next `git commit`: hook **reads → stamps → deletes** → *absent* (consume-once)
- **Rules**: keys accumulate (tokens sum across brain calls); a key absent from the sidecar ⇒ its
  trailer is omitted; env vars of the same name override the sidecar for a given key.

---

## Entity: Aggregated metrics view

Derived, read-only, from git history (`scripts/metrics.sh`).

- **Groupings**: per `Provenance-Spec`, per `Provenance-Agent`, per `Provenance-Model`.
- **Aggregates**: sum of tokens/latency/retries/errors; distribution of outcomes; existing
  commit/diff/ai-paced-share metrics.
- **Rules**:
  - Exclude omitted/`-` values from sums and averages (FR-011).
  - Per-model token sums carry a "not directly comparable across models (different tokenizers)"
    label (FR-012).
  - No network; git-only; < 5s (FR-013/SC-004).
