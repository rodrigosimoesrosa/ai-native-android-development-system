#!/usr/bin/env bash
# Enable the versioned git hooks (ADR-0010). One-time per clone.
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 2
git config core.hooksPath githooks
chmod +x githooks/* 2>/dev/null || true
echo "✓ git hooks enabled (core.hooksPath=githooks). Commits now carry auto Provenance-* trailers."
echo "  Set PROVENANCE_AGENT / PROVENANCE_METHOD / PROVENANCE_MODEL to attribute a non-human author."
