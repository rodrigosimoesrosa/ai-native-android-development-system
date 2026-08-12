# Contract: Commit-trailer schema (run metrics)

**Feature**: 006-observability-run-metrics

The contract between the `prepare-commit-msg` hook (producer) and every consumer (git log,
`scripts/metrics.sh`, `scripts/provenance.sh`, humans). Extends ADR-0010.

## Keys

```
Provenance-Tokens: <int>        # total session tokens; omitted if not measured
Provenance-Latency-Ms: <int>    # session wall-time (ms); omitted if not measured
Provenance-Retries: <int>       # per-task fix attempts; omitted if not measured
Provenance-Errors: <int>        # gate/tool failures; omitted if not measured
Provenance-Outcome: <enum>      # ok|error|cancelled; omitted if unknown (refusal/cutoff: ADR-0014)
```

Unchanged from ADR-0010 and always attempted: `Provenance-Spec`, `Provenance-Method`,
`Provenance-Agent`, `Provenance-Model`.

## Value semantics

- **Omitted key = not measured.** Consumers MUST treat absence as "unknown" and skip it (never as 0).
- **`0` = measured zero.** Valid and distinct from omitted.
- Integers are non-negative. `Provenance-Outcome` is exactly one of the enum values.
- **No content**: values are numeric/enum only — never prompt/response/context/tool data.
- **Cost**: `Provenance-Cost` is NOT emitted in this phase (reserved for a later phase; consumers
  should tolerate its future presence).

## Stamping rules (hook)

- Stamp a key only when a value is available (env var or sidecar); otherwise omit the line.
- Idempotent: if the message already carries `Provenance-Spec:` (e.g. amend), stamp nothing.
- Merge/squash messages are skipped (as today).

## Example (a stamped commit message tail)

```
feat(...): ...

Provenance-Spec: 006-observability-run-metrics
Provenance-Method: ai-paced
Provenance-Agent: opencode
Provenance-Model: ollama/llama3.1
Provenance-Tokens: 48213
Provenance-Latency-Ms: 42150
Provenance-Retries: 2
Provenance-Errors: 0
Provenance-Outcome: ok
```
