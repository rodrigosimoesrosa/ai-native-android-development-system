# adapters/claude-code/ — active adapter

Realizes the neutral [`methods/`](../../methods/) using **Claude Code + GitHub Spec Kit** (ADR-0002).
This directory is invocation-mapping only; the *how/why* stays in `methods/` and `decisions/`.

## Method → realization

| Method | Realized by |
|---|---|
| [`sdd-loop`](../../methods/sdd-loop.md) | Spec Kit skills in `.claude/skills/`: `speckit-specify` → `speckit-plan` → `speckit-tasks` → `speckit-implement` (+ `speckit-clarify`, `speckit-analyze`, `speckit-converge`). Engine + templates + memory in `.specify/`. |
| [`write-adr`](../../methods/write-adr.md) | Authored in-session following the method, using `decisions/` numbering and the ADR shape; the constitution in `.specify/memory/constitution.md` governs it. *(No dedicated skill yet.)* |
| [`verify-change`](../../methods/verify-change.md) | **In-session:** the `Stop` hook in `.claude/settings.json` → `scripts/gradle-verify.sh` (fast JVM tests). **Gate:** `.github/workflows/ci.yml` runs tests + `detekt` + the guardrail scripts (`scripts/check-knowledge.sh`, `scripts/check-adapter-boundary.sh`). |

## Run modes (ADR-0009)

The same methods run two ways; this adapter realizes both:

- **human-paced** (default) — an interactive Claude Code session; the person types the commands and
  approves gates.
- **ai-paced** — [`run-ai-paced.sh`](run-ai-paced.sh) sets Claude Code as the pluggable brain and
  hands off to the neutral harness [`scripts/ai-paced-run.sh`](../../scripts/ai-paced-run.sh), which
  runs the shared gate bounded by [`run-modes.yml`](../../run-modes.yml) and **escalates** the four
  mandatory gates. The brain is swappable — see the sibling [`adapters/opencode/`](../opencode/README.md)
  for the same loop driven by a local model. Contract: [`agents/`](agents/README.md).

## Where the tool coupling lives

- `.claude/skills/` — the skill (SKILL.md) files.
- `.claude/settings.json` — hooks (e.g. the `Stop` verification hook).
- `.specify/` — Spec Kit templates, scripts, and the constitution (memory).
- `scripts/` — the verification + guardrail commands the hooks and CI call.

Swapping to another tool = writing a sibling `adapters/<tool>/` that maps the same three methods to
that tool's mechanisms. Nothing in `methods/` or the app changes.
