# ADR-0010: Automated provenance (commit trailers) + process metrics from git

- **Status:** Accepted
- **Date:** 2026-08-10
- **Deciders:** Project maintainer
- **Related:** [constitution (Principle IV)](../docs/constitution.md), [ADR-0008 (methods/adapters)](ADR-0008-methods-and-adapters-layout.md), [ADR-0009 (run modes)](ADR-0009-run-modes-human-paced-and-ai-paced.md), [Vision §7 (metrics)](../docs/00-vision-and-architecture.md), [`methods/record-provenance`](../methods/record-provenance.md)

---

## Context

Constitution Principle IV requires recording **which spec, which method, which agent/model** produced
a change — **without polluting app code**. Today provenance is a hand-written `PROVENANCE.md` (accurate
but manual, per-feature). The vision (§7) also asks that process health be **measured, not asserted**.

## Decision

### 1. Provenance = git commit trailers
Every change records provenance as **trailers on its commit** — in git metadata, never in application
code:

```
Provenance-Spec:   001-otp-auth
Provenance-Method: ai-paced        # or a skill/stage, or "manual"
Provenance-Agent:  agent           # or "human"
Provenance-Model:  claude-sonnet-5 # or "-"
```

### 2. Auto-stamped by a versioned hook
`githooks/prepare-commit-msg` appends the trailers automatically: **Spec** derived from the branch /
matching `specs/<branch>/`; **Agent/Method/Model** from `PROVENANCE_*` environment variables,
defaulting to `human` / `manual` / `-`. Enabled per clone via `git config core.hooksPath githooks`
(`scripts/setup-hooks.sh`). The hook never double-stamps.

### 3. Queryable ledger + metrics
- `scripts/provenance.sh` renders the ledger from `git log` trailers (per spec, per agent/model).
- `scripts/metrics.sh` computes health metrics from git (commit count, diff size, files touched) —
  the vision's "measure the method".

### 4. Wired to run modes (ADR-0009)
In **ai-paced** the runner exports `PROVENANCE_*` before committing, so autonomous changes are stamped
automatically. In **human-paced** the defaults yield `human` / `manual`.

## Consequences

### Positive
- Provenance is **automatic, git-native, queryable**, and **never touches app code** (Principle IV).
- Process metrics are computed from the same source of truth (git) — no separate tracker to drift.
- Ties provenance to the run mode: ai-paced changes are self-attributing.

### Negative / costs
- Commits made **before** this ADR carry no trailers (historical gap — acceptable).
- Agent/Model come from env vars: a convention the driver (or session) must set; unset ⇒ `human`.
- The hook must be enabled per clone (`core.hooksPath`) — a one-line setup step, documented.

### Neutral
- `PROVENANCE.md` stays as a human-readable per-feature narrative; the trailers are the machine ledger.

## Alternatives considered

1. **A separate ledger file** (`provenance.jsonl`). Rejected — duplicates git history; drift risk.
2. **Keep the manual `PROVENANCE.md` only.** Rejected — not automated; Principle IV wants per-change.
3. **Provenance in code comments / annotations.** Rejected — pollutes app code, violates Principle IV.

## Resulting actions

- [x] `methods/record-provenance.md`; `githooks/prepare-commit-msg`; `scripts/{provenance,metrics,setup-hooks}.sh`.
- [x] Wire `PROVENANCE_*` export into the ai-paced harness (`scripts/ai-paced-run.sh`) and the
      per-adapter launchers.
- [ ] CI check that agent-authored commits carry provenance trailers.
- [ ] A richer metrics view (diff-size trend, onboarding-to-first-merge) when there is history to show.
