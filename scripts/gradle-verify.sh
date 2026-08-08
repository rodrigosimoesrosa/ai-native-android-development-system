#!/usr/bin/env bash
# Stop-hook verification — closes the AI-first verification loop (constitution Principle III).
#
# Fired by Claude Code's `Stop` hook at the end of each turn. It:
#   1. Reads the hook JSON on stdin and SHORT-CIRCUITS if `stop_hook_active` is true
#      (we are already inside a stop-hook continuation) — this prevents infinite loops.
#   2. Skips silently unless Kotlin/Gradle sources (.kt/.kts/.toml) changed.
#   3. Runs the fast JVM unit tests (:core, :domain).
#   4. On failure, emits {"decision":"block","reason":...} so the agent keeps fixing;
#      on success, emits a short systemMessage.
set -uo pipefail

input="$(cat)"

# (1) Break loops: if this stop was already triggered by us, permit the stop.
active="$(printf '%s' "$input" | jq -r '.stop_hook_active // false' 2>/dev/null || echo false)"
[ "$active" = "true" ] && exit 0

repo_root="$(git rev-parse --show-toplevel 2>/dev/null)" || exit 0
cd "$repo_root" || exit 0

# (2) Guard: only verify when Kotlin/Gradle sources changed (staged, unstaged, or untracked).
changed="$(git status --porcelain 2>/dev/null | grep -E '\.(kt|kts|toml)$' || true)"
[ -z "$changed" ] && exit 0

# (3) Fast JVM tests only — no emulator, no Android SDK needed for these modules.
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
log="$(mktemp)"
if ./gradlew :core:test :domain:test --console=plain >"$log" 2>&1; then
  rm -f "$log"
  echo '{"systemMessage":"✅ Principle III: JVM tests green (:core, :domain)."}'
  exit 0
fi

# (4) Failure → feed the tail back so the agent fixes it before finishing.
reason="$(printf 'Verification failed — JVM unit tests are RED (constitution Principle III: red build = broken work). Fix before finishing.\n\n%s' "$(tail -40 "$log")")"
rm -f "$log"
jq -n --arg r "$reason" '{decision:"block", reason:$r}'
exit 0
