#!/usr/bin/env bash
# T004 (US1): the commit hook stamps present run-metric keys, omits unset ones (never 0),
# preserves a measured 0, emits no content, and leaves a human/no-sidecar commit untouched (SC-006).
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

mk_repo() {
  local d; d="$(mktemp -d)"
  git -C "$d" init -q
  git -C "$d" config user.email t@example.com
  git -C "$d" config user.name tester
  git -C "$d" config core.hooksPath "$REPO_ROOT/githooks"
  printf '%s' "$d"
}
trailer() { git -C "$D" log -1 --format="%(trailers:key=$1,valueonly)" | tr -d '[:space:]'; }
has()     { [ -n "$(trailer "$1")" ]; }

# --- Case A: sidecar with some keys present, some omitted, one measured zero ---
D="$(mk_repo)"
sc="$D/.git/mirabilis"; mkdir -p "$sc"
# TOKENS present, ERRORS measured 0, OUTCOME ok; LATENCY_MS and RETRIES intentionally absent.
printf 'PROVENANCE_TOKENS=1234\nPROVENANCE_ERRORS=0\nPROVENANCE_OUTCOME=ok\n' > "$sc/run-metrics.env"
git -C "$D" commit -q --allow-empty -m "case A"

[ "$(trailer Provenance-Tokens)"  = "1234" ] || fail "A: tokens not stamped (got '$(trailer Provenance-Tokens)')"
[ "$(trailer Provenance-Errors)"  = "0" ]    || fail "A: measured 0 not preserved (got '$(trailer Provenance-Errors)')"
[ "$(trailer Provenance-Outcome)" = "ok" ]   || fail "A: outcome not stamped"
has Provenance-Latency-Ms && fail "A: omitted latency should NOT be stamped"
has Provenance-Retries    && fail "A: omitted retries should NOT be stamped"
has Provenance-Spec       || fail "A: base provenance (Spec) missing — hook broke"

# --- Case B: no sidecar, no env → no run-metric trailers (human commit unaffected, SC-006) ---
D="$(mk_repo)"
git -C "$D" commit -q --allow-empty -m "case B (human)"
has Provenance-Tokens     && fail "B: no sidecar but Tokens stamped"
has Provenance-Outcome    && fail "B: no sidecar but Outcome stamped"
has Provenance-Spec       || fail "B: base provenance missing on a normal commit"

# --- Case C: env var wins as a value source ---
D="$(mk_repo)"
PROVENANCE_TOKENS=555 git -C "$D" -c core.hooksPath="$REPO_ROOT/githooks" commit -q --allow-empty -m "case C"
[ "$(trailer Provenance-Tokens)" = "555" ] || fail "C: env-provided tokens not stamped (got '$(trailer Provenance-Tokens)')"

echo "    ok: stamps present, omits unset, preserves measured 0, human commit clean"
