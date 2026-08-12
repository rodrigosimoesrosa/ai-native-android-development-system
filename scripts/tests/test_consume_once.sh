#!/usr/bin/env bash
# T009 (US2): consume-once — the hook deletes the sidecar after stamping, so one session maps to
# exactly one commit; a second commit carries no run-metric trailers; amend never duplicates.
set -uo pipefail
REPO_ROOT="${REPO_ROOT:-$(git rev-parse --show-toplevel)}"

fail() { echo "    FAIL: $1"; exit 1; }

D="$(mktemp -d)"
git -C "$D" init -q
git -C "$D" config user.email t@example.com
git -C "$D" config user.name tester
git -C "$D" config core.hooksPath "$REPO_ROOT/githooks"

sidecar="$D/.git/mirabilis/run-metrics.env"
mkdir -p "$D/.git/mirabilis"
printf 'PROVENANCE_TOKENS=42\nPROVENANCE_OUTCOME=ok\n' > "$sidecar"

trailer() { git -C "$D" log -1 --format="%(trailers:key=$1,valueonly)" | tr -d '[:space:]'; }

# commit 1 → stamps + consumes
git -C "$D" commit -q --allow-empty -m "first (session)"
[ "$(trailer Provenance-Tokens)" = "42" ] || fail "first commit did not stamp tokens"
[ -f "$sidecar" ] && fail "sidecar not consumed (still present after stamping)"

# commit 2 → no run-metric trailers (no double counting)
git -C "$D" commit -q --allow-empty -m "second"
[ -n "$(trailer Provenance-Tokens)" ] && fail "second commit carried tokens (double counting)"

# amend → does not duplicate the provenance block
git -C "$D" commit -q --amend --no-edit --allow-empty
n="$(git -C "$D" log -1 --format='%B' | grep -c '^Provenance-Spec:')"
[ "$n" = "1" ] || fail "amend duplicated provenance (found $n Provenance-Spec lines)"

echo "    ok: consume-once → one session = one commit; amend idempotent"
