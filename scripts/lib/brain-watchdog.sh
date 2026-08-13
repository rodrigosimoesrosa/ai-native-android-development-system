#!/usr/bin/env bash
# Liveness wrapper for the ai-paced brain call (spec 007). git + bash only, no new dependency.
#
# The 005 incident was a brain call that hung and never *returned*, so the harness' bounded-retry
# never advanced and the whole run froze. This wrapper makes a stuck brain always return: it runs
# the brain in its own process group, watches its stdout for progress, and kills the whole group
# (TERM then KILL) on either an idle stall or a total-time hard-cap — returning 124 (the timeout(1)
# convention) so the caller folds it into the existing retry bound.
#
# See specs/007-ai-paced-brain-timeout/contracts/harness-liveness.md §3 and research D1/D2/D6.
#
# Portability: the idle watchdog is pure bash (macOS bash 3.2 + Linux); `stat` mtime flags differ
# (BSD `stat -f %m` vs GNU `stat -c %Y`) so we probe-and-pick. The hard-cap uses timeout(1) when
# present (with --foreground so the brain stays in our process group) and a bash-alarm otherwise.

# Read a file's mtime as epoch seconds. GNU/busybox stat uses `-c %Y`; BSD (macOS) uses `-f %m`.
# We must NOT chain them with `||`: on Linux `stat -f %m FILE` exits non-zero yet still prints a
# filesystem dump to stdout, so the fallback would append garbage and yield a non-numeric mtime
# (which the watchdog reads as "always fresh" → the idle stall is never detected). Probe once,
# keep only a numeric result, and cache the working form (probe-and-pick, D2).
_BW_STAT=""
_bw_mtime() {
  case "$_BW_STAT" in
    gnu) stat -c %Y "$1" 2>/dev/null ;;
    bsd) stat -f %m "$1" 2>/dev/null ;;
    *)
      local v
      v="$(stat -c %Y "$1" 2>/dev/null)"
      case "$v" in '' | *[!0-9]*) : ;; *) _BW_STAT=gnu; printf '%s\n' "$v"; return 0 ;; esac
      v="$(stat -f %m "$1" 2>/dev/null)"
      case "$v" in '' | *[!0-9]*) : ;; *) _BW_STAT=bsd; printf '%s\n' "$v"; return 0 ;; esac
      ;;
  esac
}

# Kill a whole process group: TERM, short grace, then KILL. No orphans survive (D6, SC-006).
# Arg is the group-leader pid, which equals the pgid for a monitor-mode job.
_bw_kill_group() {
  local pgid="$1" i=0
  [ -n "$pgid" ] || return 0
  kill -TERM -"$pgid" 2>/dev/null || true
  while kill -0 "$pgid" 2>/dev/null && [ "$i" -lt 20 ]; do
    sleep 0.1
    i=$((i + 1))
  done
  kill -KILL -"$pgid" 2>/dev/null || true
  return 0
}

# Background watchdog: poll the progress log's mtime; kill the group on idle stall, and (when the
# hard-cap is enforced here rather than by timeout(1)) on total elapsed time. Writes "124" to the
# status file before killing so the parent can distinguish a kill from a natural exit.
_bw_watchdog() {
  local idle="$1" hardcap="$2" pgid="$3" logf="$4" statusf="$5" check_hc="$6"
  local start now mt age elapsed
  start="$(date +%s)"
  while :; do
    sleep 1
    kill -0 "$pgid" 2>/dev/null || return 0   # brain gone: nothing to watch
    now="$(date +%s)"
    mt="$(_bw_mtime "$logf")"
    case "$mt" in '' | *[!0-9]*) mt="$now" ;; esac   # no readable mtime: don't idle-kill
    age=$((now - mt))
    if [ "$age" -ge "$idle" ]; then
      printf '124\n' > "$statusf"
      _bw_kill_group "$pgid"
      return 0
    fi
    if [ "$check_hc" = "1" ]; then
      elapsed=$((now - start))
      if [ "$elapsed" -ge "$hardcap" ]; then
        printf '124\n' > "$statusf"
        _bw_kill_group "$pgid"
        return 0
      fi
    fi
  done
}

# run_brain_with_liveness <idle_s> <hardcap_s> <cmd...>
#   Runs <cmd...> in its own process group, streaming its stdout through unchanged (tee'd to a
#   progress log so the idle watchdog can see activity, and to this function's stdout so callers
#   see live output). Returns 0 / N (brain's own exit) or 124 when killed for inactivity or total
#   time. Kills the whole process group on timeout (no orphans).
run_brain_with_liveness() {
  local idle="$1" hardcap="$2"
  shift 2

  local tmpdir fifo logf statusf
  tmpdir="$(mktemp -d)"
  fifo="$tmpdir/fifo"
  logf="$tmpdir/progress.log"
  statusf="$tmpdir/status"
  mkfifo "$fifo"
  : > "$logf"

  # Hard-cap mechanism: timeout(1) when present, else the watchdog's own elapsed check (bash-alarm).
  local timeout_bin="" check_hc=1
  if command -v timeout >/dev/null 2>&1; then
    timeout_bin="timeout"
    check_hc=0
  fi

  # tee runs in THIS shell's process group (not the brain's), so it survives the group-kill and
  # flushes until it sees EOF. It reads the fifo and fans out to the progress log + our stdout.
  tee "$logf" < "$fifo" &
  local teepid=$!

  # Launch the brain in its OWN process group via monitor mode, so we can signal the whole group.
  # $! is the group-leader pid (== pgid). --foreground keeps the brain in that group under timeout(1)
  # so the idle-kill reaches it; timeout(1) then only TERMs its direct child, and our group-kill in
  # cleanup reaps any descendants (e.g. a spawned `sleep`) it left behind.
  local had_m=0
  case "$-" in *m*) had_m=1 ;; esac
  local pgid
  set -m
  if [ -n "$timeout_bin" ]; then
    { "$timeout_bin" --foreground -k 3 -s TERM "$hardcap" "$@" > "$fifo" 2>&1; } &
  else
    { "$@" > "$fifo" 2>&1; } &
  fi
  pgid=$!
  [ "$had_m" = 1 ] || set +m

  # Watch for an idle stall (and the hard-cap when timeout(1) is absent).
  _bw_watchdog "$idle" "$hardcap" "$pgid" "$logf" "$statusf" "$check_hc" &
  local wdpid=$!

  # Wait for the brain to finish (naturally, or because a watcher killed its group).
  local rc=0
  wait "$pgid" 2>/dev/null || rc=$?

  # Tear down the watchdog and guarantee the group is gone (reaps any timeout(1)-orphaned children).
  kill "$wdpid" 2>/dev/null || true
  wait "$wdpid" 2>/dev/null || true
  _bw_kill_group "$pgid"

  # Drain tee (EOF arrives once every writer on the fifo is dead), then clean up.
  wait "$teepid" 2>/dev/null || true

  # A watcher-initiated kill is a timeout regardless of the brain's signalled exit code.
  if [ -s "$statusf" ]; then
    rc=124
  fi

  rm -rf "$tmpdir"
  return "$rc"
}
