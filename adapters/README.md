# adapters/ — the thin, disposable tool layer

An **adapter** realizes the neutral [`methods/`](../methods/) for one specific AI tool. It holds
**only packaging/invocation** — which command, which skill file, which hook — **never** method logic or
project knowledge (ADR-0001, ADR-0008).

**Golden rule:** if it holds *how or why*, it belongs in the neutral core (`methods/`, `decisions/`,
`docs/`, the app). If it holds *"to do X in tool T, invoke Y"*, it belongs here.

**Swap cost:** switching AI tools means writing a new directory under `adapters/`. The neutral core —
`methods/`, `specs/`, `decisions/`, `docs/`, the Android app, and the tests — is untouched. That is the
whole portability guarantee, made concrete.

## Adapters

| Adapter | Status | Realizes |
|---|---|---|
| [`claude-code/`](claude-code/) | **active** | all methods, via Spec Kit skills, hooks, and CI scripts |
| [`opencode/`](opencode/) | **planned** (swap target) | documents what an equivalent adapter provides |

The second (planned) adapter is not decoration: its presence is the falsifiable claim that
tool-neutrality is real and not asserted. A CI guardrail keeps `methods/` free of any tool token, so
the boundary cannot silently rot.
