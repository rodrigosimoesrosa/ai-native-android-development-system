#!/usr/bin/env bash
# Process health metrics from git (ADR-0010; vision §7 — measure, don't assert). Read-only.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

commits="$(git rev-list --count HEAD 2>/dev/null || echo 0)"
echo "Process metrics (from git history):"
echo "  commits: $commits"

git log --pretty=tformat: --numstat 2>/dev/null | awk -v commits="$commits" '
  $1 ~ /^[0-9]+$/ { add += $1 }
  $2 ~ /^[0-9]+$/ { del += $2 }
  { files++ }
  END {
    if (files > 0) {
      printf "  lines added: %d, removed: %d\n", add, del
      avg = (commits > 0) ? files / commits : 0
      printf "  file-changes: %d (avg %.1f per commit)\n", files, avg
    }
  }'

echo "  commits per spec (Provenance-Spec):"
out="$(git log --format='%(trailers:key=Provenance-Spec,valueonly)' 2>/dev/null | grep . | sort | uniq -c | sort -rn)"
if [ -n "$out" ]; then printf '%s\n' "$out" | sed 's/^/    /'; else echo "    (no provenance trailers yet — run scripts/setup-hooks.sh)"; fi
