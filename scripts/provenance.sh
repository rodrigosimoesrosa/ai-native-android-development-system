#!/usr/bin/env bash
# Render the provenance ledger from commit trailers (ADR-0010). Read-only.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

total="$(git rev-list --count HEAD 2>/dev/null || echo 0)"
stamped="$(git log --format='%(trailers:key=Provenance-Spec,valueonly)' 2>/dev/null | grep -c . || true)"

# Honest coverage: the process can only stamp commits made AFTER provenance was enabled, and the hook
# deliberately skips merges. So the meaningful denominator is non-merge commits since the baseline (the
# oldest commit that carries a trailer) — all-time coverage conflates un-stampable pre-provenance history.
base="$(git log --reverse --format='%H' --grep='^Provenance-Spec:' 2>/dev/null | head -1)"
if [ -n "$base" ]; then
  elig="$(git rev-list --no-merges --count "${base}^..HEAD" 2>/dev/null || echo 0)"
  cov="$(git log --no-merges "${base}^..HEAD" --format='%(trailers:key=Provenance-Spec,valueonly)' 2>/dev/null | grep -c . || true)"
  pct="$(awk -v c="${cov:-0}" -v e="${elig:-0}" 'BEGIN { printf "%d", (e>0) ? (100*c/e)+0.5 : 0 }')"
fi

echo "Provenance ledger — $stamped / $total commits carry provenance trailers (all-time)."
if [ -n "$base" ]; then
  echo "  since provenance enabled (baseline ${base:0:7}): $cov / $elig non-merge commits stamped (${pct}%)."
fi
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
