#!/usr/bin/env bash
# "Docs are tested" + cold-start-lite self-sufficiency (Constitution §8; vision open-question #6):
# verifies the knowledge layer is present and its internal markdown links resolve — so an agent
# given only `git clone` has complete, non-broken context. A broken link is a broken build.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2
fail=0

# 1) Essential knowledge artifacts must exist.
required=(
  README.md
  index.md
  docs/00-vision-and-architecture.md
  .specify/memory/constitution.md
  decisions/ADR-0001-build-on-existing-tools-neutral-core.md
)
for f in "${required[@]}"; do
  [ -f "$f" ] || { echo "✗ missing knowledge file: $f"; fail=1; }
done

# 1b) Every feature spec carries the full loop (spec → plan → tasks).
for d in specs/*/; do
  [ -d "$d" ] || continue
  for f in spec.md plan.md tasks.md; do
    [ -f "$d$f" ] || { echo "✗ $d missing $f"; fail=1; }
  done
done

# 2) Intra-repo markdown links resolve (neutral knowledge docs only; skip the tooling adapters).
broken="$(mktemp)"
while IFS= read -r md; do
  while IFS= read -r link; do
    case "$link" in
      http*|mailto:*|\#*) continue ;;
    esac
    target="${link%%#*}"          # strip #anchor
    [ -z "$target" ] && continue
    resolved="$(dirname "$md")/$target"
    [ -e "$resolved" ] || echo "✗ broken link in $md -> $link" >> "$broken"
  done < <(grep -oE '\]\([^)]+\)' "$md" | sed -E 's/^\]\(//; s/\)$//')
done < <(find . -name '*.md' \
  -not -path './.git/*' -not -path '*/build/*' \
  -not -path './.claude/*' -not -path './.specify/*' \
  -not -path './mvi-sample/*' -not -path './.obsidian/*')

if [ -s "$broken" ]; then cat "$broken"; fail=1; fi
rm -f "$broken"

if [ "$fail" -ne 0 ]; then
  echo "Knowledge check FAILED."
  exit 1
fi
echo "✓ Knowledge layer present and all internal links resolve."
