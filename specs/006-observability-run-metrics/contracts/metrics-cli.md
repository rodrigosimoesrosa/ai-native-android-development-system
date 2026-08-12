# Contract: metrics.sh output (run metrics)

**Feature**: 006-observability-run-metrics

Extends the existing `scripts/metrics.sh` contract (ADR-0010). Read-only, git-only, no network.

## Invocation

```
scripts/metrics.sh              # human-readable text (terminal / CI logs)
scripts/metrics.sh --markdown   # GitHub-flavored markdown (CI job summary / artifact)
```

## Additions (beyond existing commit/diff/ai-paced-share output)

- **Totals** across stamped history: sum of `Provenance-Tokens`, `Provenance-Latency-Ms`,
  `Provenance-Retries`, `Provenance-Errors`; **outcome distribution** from `Provenance-Outcome`.
- **Breakdowns**: tokens (and outcome) **per spec**, **per agent**, **per model**.
- Every per-model token figure is annotated: *"approximate — not directly comparable across models
  (different tokenizers)"*.

## Rules

- Omitted / `-` values are excluded from sums and averages (reuses the existing blank/`-` skip).
- When both a total and a future split are present, the total wins (forward-compat; ADR-0013 §1).
- Deterministic: same history → same output. Completes in < 5s on this repo (SC-004).
- Exit non-zero only on a real error (e.g. not a git repo); an empty ledger prints a hint, not a
  failure (matches current behavior).
