# ai-paced agent contract

The [`ai-paced-run.sh`](../../../scripts/ai-paced-run.sh) harness closes the loop but keeps the **brain pluggable**.
On a RED verification gate it invokes `$AI_PACED_AGENT_CMD` and then re-runs the gate — up to
`max_fix_iterations` (from [`run-modes.yml`](../../../run-modes.yml)).

## Contract

`AI_PACED_AGENT_CMD` is any command. When invoked it receives, in the environment:

| Var | Meaning |
|---|---|
| `TASKS` | path to the approved `tasks.md` being executed |
| `NEXT_TASK` | the next unchecked task line to implement |
| `GATE_LOG` | current failing-checks output (what to fix, if anything) |
| `SPEC` | the current spec / branch being worked |

It must **implement `NEXT_TASK`** by editing the working tree, and **mark that task `[x]` in `TASKS`**
once it passes. If the gate is red it fixes the failures (`GATE_LOG`). It must **not commit** — the
harness re-runs the gate and, when the list is complete and green, opens the change request (the human
merge gate still applies). Its exit code is advisory: the gate + the task list, not the agent, are the
source of truth.

## Plug a real agent (headless Claude Code)

```bash
export PROVENANCE_MODEL=claude-sonnet-5
export AI_PACED_AGENT_CMD='claude -p "Read the failing checks in $GATE_LOG and fix them for spec \
  $SPEC by editing files in this repo. Do not commit. Keep changes minimal and in-scope."'
bash scripts/ai-paced-run.sh
# …or simply: bash adapters/claude-code/run-ai-paced.sh   (sets the Claude brain for you)
```

Any other agent (opencode, a custom script) plugs in the same way — the harness, gate, and escalation
are unchanged. That is the neutral-core/adapter guarantee applied to the *brain* itself.

## Mock agent (for demonstrating the closed loop)

A trivial deterministic "agent" is enough to prove the loop closes end-to-end without an LLM — e.g. a
command that repairs a seeded failure the gate detects. The harness treats it identically to a real
agent: RED → agent edits → re-run gate → GREEN → open change request.
