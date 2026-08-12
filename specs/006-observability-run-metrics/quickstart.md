# Quickstart & Validation: Per-Commit Run Metrics

**Feature**: 006-observability-run-metrics | **Date**: 2026-08-11

Prove the feature end-to-end without any new dependency (git + bash only).

## Prerequisites

- Provenance hook enabled for the clone: `scripts/setup-hooks.sh` (sets `core.hooksPath=githooks`).
- Contracts: [trailer-schema](contracts/trailer-schema.md), [sidecar-format](contracts/sidecar-format.md),
  [metrics-cli](contracts/metrics-cli.md).

## Validate — automated (authoritative, Principle III)

```bash
bash scripts/tests/run.sh
```

Expected: all cases green —
- hook stamps present keys, **omits** unset ones, is **idempotent**, and **consumes** (deletes) the
  sidecar (one-session→one-commit);
- a measured `0` survives and stays distinct from omitted;
- `metrics.sh` excludes `-`, breaks down per spec/agent/model, prints the tokenizer caveat.

Boundary + knowledge guardrails (existing gate):

```bash
bash scripts/check-adapter-boundary.sh
bash scripts/check-knowledge.sh
```

## Validate — manual smoke (simulating a session)

```bash
# 1. Simulate a session writing its sidecar (normally done by the harness + adapter):
d="$(git rev-parse --git-dir)/mirabilis"; mkdir -p "$d"
printf 'PROVENANCE_TOKENS=1234\nPROVENANCE_LATENCY_MS=5000\nPROVENANCE_RETRIES=1\nPROVENANCE_ERRORS=0\nPROVENANCE_OUTCOME=ok\n' > "$d/run-metrics.env"

# 2. Make a commit → hook stamps the trailers and deletes the sidecar:
git commit --allow-empty -m "test: run-metrics smoke"
git log -1 --format='%(trailers)'        # → shows Provenance-Tokens/Latency-Ms/Retries/Errors/Outcome
test ! -f "$d/run-metrics.env" && echo "sidecar consumed ✓"

# 3. A second commit carries NO run-metric trailers (consume-once):
git commit --allow-empty -m "test: second commit"
git log -1 --format='%(trailers)'        # → no Provenance-Tokens line

# 4. Aggregate view:
scripts/metrics.sh                        # tokens/outcome per spec/agent/model; tokenizer caveat
```

## Acceptance mapping

| Spec item | Validated by |
|---|---|
| FR-001/002 capture tokens + run-health | manual smoke; real run via adapter/harness |
| FR-003 / SC-002 one-session→one-commit | `test_hook_trailers.sh` (consume-once); smoke step 3 |
| FR-004/005 / SC-005 omit vs measured `0` | `test_hook_trailers.sh` |
| FR-006/007 / SC-003 git-native, content-free | trailer inspection; no content keys exist |
| FR-008 idempotent stamping | `test_hook_trailers.sh` (amend case) |
| FR-010/011/012 aggregation | `test_metrics_agg.sh`; `metrics.sh` output |
| FR-013 / SC-004 read-only, no network, <5s | `metrics.sh` run; test asserts no network |
| FR-014 boundary | `check-adapter-boundary.sh`; tokens only in adapter files |
| SC-006 human commit unaffected | smoke: a plain commit with no sidecar carries no run-metric keys |

## Out of scope (do not add here)

- `Provenance-Cost` / USD, a price table (later phase).
- Traces, prompt/response logging, evals, any external telemetry sink.
