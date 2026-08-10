#!/usr/bin/env bash
# ADR-0001 guardrail (vision open-question #7): the neutral core must not couple to a specific AI
# tool. The Android app — the proving ground — is checked here: its Kotlin/Gradle sources must
# contain ZERO references to the AI tooling (Claude Code, Spec Kit, adapters). Tool coupling lives
# only in the disposable adapter (`.claude/`, `.specify/`), never in the project's intelligence.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

dirs=(core core-ui domain data feature app)
# Tokens that would signal app code reaching into the AI-tooling adapter layer.
pattern='claude|speckit|spec-kit|SKILL\.md|\.specify|/adapters/|anthropic'

hits="$(grep -rInE -i "$pattern" "${dirs[@]}" \
  --include='*.kt' --include='*.kts' 2>/dev/null || true)"

if [ -n "$hits" ]; then
  echo "✗ Neutral-core/adapter boundary violated (ADR-0001):"
  echo "  app/library code must not reference the AI tooling. Offending lines:"
  echo "$hits"
  exit 1
fi

echo "✓ App & library code is tool-neutral — no AI-tooling references (ADR-0001)."
