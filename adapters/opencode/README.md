# adapters/opencode/ — ai-paced with local models

**Status:** active for the **ai-paced brain** (running a local LLM); the full SDD-loop commands remain
a v3 item. This is the ADR-0001 portability guarantee **proven by execution**: the same harness
(`scripts/ai-paced-run.sh`), gate, escalation, and provenance run with a *different brain* — only this
directory is authored (ADR-0008, ADR-0009).

## What this gives you

Run [`ai-paced`](../../methods/run-modes.md) with a **fully local** model — [Ollama](https://ollama.com)
or [LM Studio](https://lmstudio.ai) — as the autonomous fix loop's brain, via
[`opencode`](https://opencode.ai). No cloud, no API key.

## Setup

1. **Install opencode:** `curl -fsSL https://opencode.ai/install | bash`
2. **Start a local model server with a ≥ 64k context window** (opencode needs it for file-editing tools):
   - **Ollama:** `ollama serve` then `ollama pull qwen2.5-coder:7b` (set the context length to 64k+).
     OpenAI-compatible endpoint: `http://localhost:11434/v1`.
   - **LM Studio:** load a coder model, *Developer → Start Local Server*. Endpoint: `http://localhost:1234/v1`.
3. **(Optional) config:** [`opencode.example.json`](opencode.example.json) shows both providers; the
   launcher writes a project-root `opencode.json` (gitignored) for you if none exists.

## Run

```bash
# Ollama (default)
PROVIDER=ollama   MODEL=qwen2.5-coder:7b bash adapters/opencode/run-ai-paced.sh
# LM Studio
PROVIDER=lmstudio MODEL=qwen2.5-coder    bash adapters/opencode/run-ai-paced.sh
```

[`run-ai-paced.sh`](run-ai-paced.sh) checks the tool + server, points opencode at the local endpoint,
sets `AI_PACED_AGENT_CMD='opencode run --model <provider>/<model> "…"'` (+ provenance), and hands off
to the neutral harness [`scripts/ai-paced-run.sh`](../../scripts/ai-paced-run.sh).

## Honest limits

- A small local model may not reliably fix real failures — the harness **bounds retries and reports**
  (`max_fix_iterations` in [`run-modes.yml`](../../run-modes.yml)); it never hangs.
- The four mandatory human gates still apply (merge / architecture / dependency / release are escalated).
- The agent contract is in [`../claude-code/agents/README.md`](../claude-code/agents/README.md) — it is
  brain-agnostic.
