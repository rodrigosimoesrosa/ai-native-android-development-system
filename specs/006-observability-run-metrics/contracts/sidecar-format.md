# Contract: Session sidecar file

**Feature**: 006-observability-run-metrics

The contract between the writers (neutral harness + tool adapter) and the consumer (the
`prepare-commit-msg` hook). See research D1.

## Location

```
"$(git rev-parse --git-dir)"/mirabilis/run-metrics.env
```

Inside `.git/` → never tracked, never in the working tree, survives across shells in the same clone.
Writers MUST `mkdir -p` the `mirabilis/` dir. A shared helper `scripts/lib/run-metrics.sh` (neutral)
exposes the path and append/read functions so harness, adapters, and the hook agree.

## Format

Plain `KEY=VALUE` lines; keys use the `PROVENANCE_*` names:

```
PROVENANCE_TOKENS=48213
PROVENANCE_LATENCY_MS=42150
PROVENANCE_RETRIES=2
PROVENANCE_ERRORS=0
PROVENANCE_OUTCOME=ok
```

## Rules

- **Append/accumulate**: `PROVENANCE_TOKENS` is summed across brain invocations in a session; the
  writer updates the running total.
- **Absent key ⇒ omitted trailer** (not measured). Writers MUST NOT write a fabricated or `0` value
  for something they could not measure.
- **Precedence**: at commit time an exported env var of the same name overrides the sidecar value for
  that key; otherwise the sidecar value is used.
- **Consume-once**: the hook reads all keys, stamps present ones, then **deletes** the file. Only the
  first commit after a session is stamped (implements one-session→one-commit).
- **Safety**: the hook parses `KEY=VALUE` defensively (no `source` of untrusted content); only the
  known keys are honored; values validated as int/enum before stamping.

## Writers (who sets what)

| Key | Writer | Boundary |
|---|---|---|
| `PROVENANCE_TOKENS` | `adapters/<tool>/run-ai-paced.sh` | tool-specific (Principle V) |
| `PROVENANCE_LATENCY_MS` / `_RETRIES` / `_ERRORS` / `_OUTCOME` | `scripts/ai-paced-run.sh` | neutral harness |
