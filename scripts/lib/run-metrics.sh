#!/usr/bin/env bash
# Neutral helper for per-commit run metrics (spec 006; ADR-0013 + ADR-0014 §3). git + bash only.
#
# The sidecar lives INSIDE .git/ (never tracked, never in the working tree) and bridges a session
# (which does not commit) to the next commit (which does). Writers: the harness writes run-health,
# the tool adapter writes tokens. Reader/consumer: githooks/prepare-commit-msg.
#
# Content-free by construction: only numeric/enum values — never prompt/response/context.

# Env-var names carried through the sidecar and stamped as trailers by the hook.
RUN_METRICS_KEYS="PROVENANCE_TOKENS PROVENANCE_LATENCY_MS PROVENANCE_RETRIES PROVENANCE_ERRORS PROVENANCE_OUTCOME"

run_metrics_sidecar_path() {
  local gd
  gd="$(git rev-parse --git-dir 2>/dev/null)" || return 1
  printf '%s/mirabilis/run-metrics.env\n' "$gd"
}

# Validate a value: numeric keys → non-negative integer; PROVENANCE_OUTCOME → the Phase-A enum.
# (refusal/cutoff are model-level and deferred to ADR-0014.)
run_metrics_valid() {
  local key="$1" val="$2"
  case "$key" in
    PROVENANCE_OUTCOME)
      case "$val" in ok|error|cancelled|timeout) return 0 ;; *) return 1 ;; esac ;;
    PROVENANCE_TOKENS|PROVENANCE_LATENCY_MS|PROVENANCE_RETRIES|PROVENANCE_ERRORS)
      case "$val" in '' | *[!0-9]*) return 1 ;; *) return 0 ;; esac ;;
    *) return 1 ;;
  esac
}

# Overwrite (set) a key in the sidecar. Ignores unknown/invalid values (never fabricates).
run_metrics_set() {
  local key="$1" val="$2" f tmp
  run_metrics_valid "$key" "$val" || return 1
  f="$(run_metrics_sidecar_path)" || return 1
  mkdir -p "$(dirname "$f")" || return 1
  tmp="$(mktemp)" || return 1
  [ -f "$f" ] && grep -v "^$key=" "$f" > "$tmp" 2>/dev/null
  printf '%s=%s\n' "$key" "$val" >> "$tmp"
  mv "$tmp" "$f"
}

# Accumulate tokens across brain invocations in a session (sum). Omits on non-numeric input.
run_metrics_add_tokens() {
  local add="$1" f cur=0
  case "$add" in '' | *[!0-9]*) return 1 ;; esac
  f="$(run_metrics_sidecar_path)" || return 1
  if [ -f "$f" ]; then
    cur="$(grep '^PROVENANCE_TOKENS=' "$f" | tail -1 | cut -d= -f2)"
  fi
  case "$cur" in '' | *[!0-9]*) cur=0 ;; esac
  run_metrics_set PROVENANCE_TOKENS "$((cur + add))"
}

# Get a key's value: an exported env var of the same name wins; otherwise the sidecar. Empty if none.
run_metrics_get() {
  local key="$1" ev f v
  eval "ev=\${$key:-}"
  if [ -n "$ev" ]; then printf '%s' "$ev"; return 0; fi
  f="$(run_metrics_sidecar_path)" || return 0
  [ -f "$f" ] || return 0
  v="$(grep "^$key=" "$f" | tail -1 | cut -d= -f2)"
  printf '%s' "$v"
}

# Consume-once: delete the sidecar after stamping, so one session maps to exactly one commit.
run_metrics_consume() {
  local f
  f="$(run_metrics_sidecar_path)" || return 0
  rm -f "$f" 2>/dev/null || true
}
