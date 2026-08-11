#!/usr/bin/env bash
# Process health metrics from git (ADR-0010; vision §7 — measure, don't assert). Read-only.
# Usage: scripts/metrics.sh [--markdown]
#   default   — human-readable text (for a terminal / CI logs)
#   --markdown — GitHub-flavored markdown (for a CI job summary / artifact)
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2

fmt="text"
[ "${1:-}" = "--markdown" ] && fmt="markdown"

commits="$(git rev-list --count HEAD 2>/dev/null || echo 0)"

# Diff totals across history.
read -r add del files <<EOF
$(git log --pretty=tformat: --numstat 2>/dev/null | awk '
  $1 ~ /^[0-9]+$/ { add += $1 }
  $2 ~ /^[0-9]+$/ { del += $2 }
  { files++ }
  END { printf "%d %d %d", add+0, del+0, files+0 }')
EOF
avg="$(awk -v f="$files" -v c="$commits" 'BEGIN { printf "%.1f", (c>0) ? f/c : 0 }')"

# Provenance trailer breakdowns (ADR-0010). Blank/"-" values are ignored.
trailer_counts() { # $1 = trailer key → "  <count> <value>" lines, most frequent first
  git log --format="%(trailers:key=Provenance-$1,valueonly)" 2>/dev/null \
    | sed '/^[[:space:]]*$/d; /^-$/d' | sort | uniq -c | sort -rn
}
per_spec="$(trailer_counts Spec)"
per_agent="$(trailer_counts Agent)"
per_method="$(trailer_counts Method)"

# ai-paced share = ai-paced commits / commits that carry a Method trailer.
ai_paced="$(git log --format='%(trailers:key=Provenance-Method,valueonly)' 2>/dev/null | grep -c '^ai-paced$' || true)"
stamped="$(git log --format='%(trailers:key=Provenance-Method,valueonly)' 2>/dev/null | sed '/^[[:space:]]*$/d; /^-$/d' | grep -c . || true)"
share="$(awk -v a="${ai_paced:-0}" -v s="${stamped:-0}" 'BEGIN { printf "%d", (s>0) ? (100*a/s)+0.5 : 0 }')"

if [ "$fmt" = "markdown" ]; then
  block() { # $1 = title, $2 = counts text
    printf '\n**%s**\n\n' "$1"
    if [ -n "$2" ]; then
      printf '| Value | Commits |\n|---|--:|\n'
      printf '%s\n' "$2" | awk '{ c=$1; $1=""; sub(/^ /,""); printf "| %s | %d |\n", $0, c }'
    else
      printf '_(no trailers yet — enable the hook: `scripts/setup-hooks.sh`)_\n'
    fi
  }
  {
    printf '## Process metrics (from git history)\n\n'
    printf '| Metric | Value |\n|---|--:|\n'
    printf '| Commits | %d |\n' "$commits"
    printf '| Lines added / removed | %d / %d |\n' "$add" "$del"
    printf '| File-changes | %d (avg %s per commit) |\n' "$files" "$avg"
    printf '| ai-paced share | %d%% of stamped commits |\n' "$share"
    block "Commits per spec" "$per_spec"
    block "Commits per agent" "$per_agent"
    block "Commits per method" "$per_method"
  }
  exit 0
fi

# --- text ---
echo "Process metrics (from git history):"
echo "  commits: $commits"
echo "  lines added: $add, removed: $del"
echo "  file-changes: $files (avg $avg per commit)"
echo "  ai-paced share: ${share}% of stamped commits"
emit() { # $1 = label, $2 = counts
  echo "  $1:"
  if [ -n "$2" ]; then printf '%s\n' "$2" | sed 's/^/    /'; else echo "    (none — run scripts/setup-hooks.sh)"; fi
}
emit "commits per spec (Provenance-Spec)" "$per_spec"
emit "commits per agent (Provenance-Agent)" "$per_agent"
emit "commits per method (Provenance-Method)" "$per_method"
