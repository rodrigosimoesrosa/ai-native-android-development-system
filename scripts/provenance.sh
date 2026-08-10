#!/usr/bin/env bash
# Render the provenance ledger from commit trailers (ADR-0010). Read-only.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

total="$(git rev-list --count HEAD 2>/dev/null || echo 0)"
stamped="$(git log --format='%(trailers:key=Provenance-Spec,valueonly)' 2>/dev/null | grep -c . || true)"

echo "Provenance ledger — $stamped / $total commits carry provenance trailers."
echo

show() { # $1 = trailer key, $2 = label
  local out
  out="$(git log --format="%(trailers:key=$1,valueonly)" 2>/dev/null | grep . | sort | uniq -c | sort -rn)"
  echo "By $2:"
  if [ -n "$out" ]; then printf '%s\n' "$out" | sed 's/^/  /'; else echo "  (none yet)"; fi
}

show Provenance-Agent agent
show Provenance-Model model
show Provenance-Spec  spec
