#!/usr/bin/env bash
# Dependency-free test runner for the meta-system shell tooling (spec 006). git + bash only.
# Runs every scripts/tests/test_*.sh in isolation; each spins up its own throwaway git repo.
set -uo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)" || { echo "not a git repo"; exit 2; }
export REPO_ROOT
cd "$REPO_ROOT"

pass=0; fail=0; failed=""
for t in scripts/tests/test_*.sh; do
  [ -f "$t" ] || continue
  name="$(basename "$t")"
  if bash "$t"; then
    echo "  ✓ $name"; pass=$((pass + 1))
  else
    echo "  ✗ $name"; fail=$((fail + 1)); failed="$failed $name"
  fi
done

echo "── tests: $pass passed, $fail failed ──"
if [ "$fail" -ne 0 ]; then
  echo "FAILED:$failed"
  exit 1
fi
