#!/usr/bin/env bash
# ADR-0001 / ADR-0008 guardrail (vision open-question #7): the neutral core must not couple to a
# specific AI tool. Two checks:
#   (1) the Android app/library sources contain ZERO AI-tooling references;
#   (2) methods/ (the neutral "how") names NO tool at all.
# Tool coupling lives only in the disposable adapters (`adapters/`, `.claude/`, `.specify/`); decisions/
# and docs/ may *name* a tool when the decision is about tooling (they describe, never invoke).
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2
fail=0

# (1) App & library code must be tool-neutral.
app_dirs=(core core-ui domain data feature app)
app_pattern='claude|speckit|spec-kit|SKILL\.md|\.specify|/adapters/|anthropic'
app_hits="$(grep -rInE -i "$app_pattern" "${app_dirs[@]}" \
  --include='*.kt' --include='*.kts' 2>/dev/null || true)"
if [ -n "$app_hits" ]; then
  echo "✗ ADR-0001: app/library code references the AI tooling:"
  echo "$app_hits"
  fail=1
fi

# (2) methods/ must be fully tool-neutral (name no tool). "adapters" as a concept is allowed.
if [ -d methods ]; then
  methods_pattern='claude|opencode|speckit|spec-kit|SKILL\.md|\.specify'
  methods_hits="$(grep -rInE -i "$methods_pattern" methods --include='*.md' 2>/dev/null || true)"
  if [ -n "$methods_hits" ]; then
    echo "✗ ADR-0008: methods/ must name no tool (move tool specifics to adapters/):"
    echo "$methods_hits"
    fail=1
  fi
fi

if [ "$fail" -ne 0 ]; then
  echo "Neutral-core/adapter boundary FAILED."
  exit 1
fi
echo "✓ Neutral core is tool-neutral: app/library code + methods/ carry no AI-tooling coupling (ADR-0001/0008)."
