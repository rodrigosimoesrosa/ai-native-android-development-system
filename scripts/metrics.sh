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

# Run metrics (spec 006; ADR-0013 + ADR-0014 §3). Content-free. Blank / "-" (not-measured) skipped.
sum_trailer() { # $1 = key suffix → integer sum, ignoring blank/"-"/non-numeric
  git log --format="%(trailers:key=Provenance-$1,valueonly)" 2>/dev/null \
    | sed '/^[[:space:]]*$/d; /^-$/d' \
    | awk '{ if ($1 ~ /^[0-9]+$/) s+=$1 } END { printf "%d", s+0 }'
}
tok_total="$(sum_trailer Tokens)"
lat_total="$(sum_trailer Latency-Ms)"
ret_total="$(sum_trailer Retries)"
err_total="$(sum_trailer Errors)"
per_outcome="$(trailer_counts Outcome)"

# Tokens per model — approximate: counts are NOT directly comparable across models (tokenizers differ).
tokens_by_model() {
  git log --format='%H' 2>/dev/null | while IFS= read -r _h; do
    _m="$(git log -1 "$_h" --format='%(trailers:key=Provenance-Model,valueonly)' 2>/dev/null | sed '/^[[:space:]]*$/d; /^-$/d' | head -1)"
    _t="$(git log -1 "$_h" --format='%(trailers:key=Provenance-Tokens,valueonly)' 2>/dev/null | sed '/^[[:space:]]*$/d; /^-$/d' | head -1)"
    [ -n "$_m" ] || continue
    [ -n "$_t" ] || continue
    case "$_t" in *[!0-9]*) continue ;; esac
    printf '%s\t%s\n' "$_t" "$_m"
  done | awk -F'\t' '{ a[$2]+=$1 } END { for (k in a) printf "  %d %s\n", a[k], k }' | sort -k2
}
per_model_tokens="$(tokens_by_model)"

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
    printf '\n**Run metrics** (spec 006 · content-free)\n\n'
    printf '| Signal | Total |\n|---|--:|\n'
    printf '| Tokens | %d |\n' "$tok_total"
    printf '| Latency (ms) | %d |\n' "$lat_total"
    printf '| Retries | %d |\n' "$ret_total"
    printf '| Errors | %d |\n' "$err_total"
    block "Outcome distribution" "$per_outcome"
    if [ -n "$per_model_tokens" ]; then
      printf '\n**Tokens per model** _(approximate — not directly comparable across models: different tokenizers)_\n\n'
      printf '| Model | Tokens |\n|---|--:|\n'
      printf '%s\n' "$per_model_tokens" | awk '{ t=$1; $1=""; sub(/^ /,""); printf "| %s | %d |\n", $0, t }'
    fi
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
echo "  run metrics (spec 006):"
echo "    tokens: $tok_total, latency-ms: $lat_total, retries: $ret_total, errors: $err_total"
emit "outcome distribution (Provenance-Outcome)" "$per_outcome"
if [ -n "$per_model_tokens" ]; then
  echo "    tokens per model (approximate — not directly comparable across models: different tokenizers):"
  printf '%s\n' "$per_model_tokens" | sed 's/^/    /'
fi

exit 0
