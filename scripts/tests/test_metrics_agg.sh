#!/usr/bin/env bash
# T011 (US3): metrics.sh aggregates tokens/outcome per spec/agent/model, excludes not-measured (-),
# prints the tokenizer caveat, emits no Provenance-Cost (deferred, FR-009), and makes no network call.
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

D="$(mktemp -d)"
git -C "$D" init -q
git -C "$D" config user.email t@example.com
git -C "$D" config user.name tester
# No hooksPath here: we craft trailers directly for controlled aggregation input.

commit() { git -C "$D" commit -q --allow-empty -m "$1"; }

commit "c1

Provenance-Spec: 006-observability-run-metrics
Provenance-Method: ai-paced
Provenance-Agent: opencode
Provenance-Model: ollama/llama3
Provenance-Tokens: 100
Provenance-Outcome: ok"

commit "c2

Provenance-Spec: 006-observability-run-metrics
Provenance-Method: ai-paced
Provenance-Agent: claude-code
Provenance-Model: claude-sonnet-5
Provenance-Tokens: 200
Provenance-Errors: 2
Provenance-Outcome: error"

# c3: no Tokens trailer at all → must be EXCLUDED from the sum (not treated as 0).
commit "c3

Provenance-Spec: 006-observability-run-metrics
Provenance-Method: ai-paced
Provenance-Agent: opencode
Provenance-Model: ollama/llama3
Provenance-Outcome: ok"

out="$(cd "$D" && bash "$REPO_ROOT/scripts/metrics.sh" 2>&1)" || fail "metrics.sh exited non-zero"

echo "$out" | grep -q "300" || fail "token total 300 (100+200, excluding the unmeasured c3) not found"
echo "$out" | grep -q "ollama/llama3" || fail "per-model breakdown missing ollama/llama3"
echo "$out" | grep -q "claude-sonnet-5" || fail "per-model breakdown missing claude-sonnet-5"
echo "$out" | grep -qiE "tokeniz|not directly comparable" || fail "tokenizer caveat missing"
echo "$out" | grep -qi "Provenance-Cost" && fail "Provenance-Cost must NOT appear (deferred, FR-009)"

# No network: the script must not call network tools.
grep -qE '\b(curl|wget|nc|ping)\b' "$REPO_ROOT/scripts/metrics.sh" && fail "metrics.sh contains a network call"

echo "    ok: aggregates, excludes '-', tokenizer caveat, no cost, offline"
