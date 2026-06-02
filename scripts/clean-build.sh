#!/usr/bin/env bash
#
# clean-build.sh — clean + build the PvP Enhancer plugin.
#
#   1. Removes any side-loaded pvp-enhancer jar from ~/.runelite/sideloaded-plugins
#      (prevents the duplicate-plugin issue when running `./gradlew run`, which loads the
#      plugin from the classpath; a leftover side-loaded jar would load it a second time).
#   2. Runs `./gradlew build`.
#
# Run manually:  bash scripts/clean-build.sh
# Also wired as a Stop hook (.claude/settings.local.json) to run after each change.
#
# Exit codes: 0 = build OK (or skipped because already running); 2 = build FAILED.
#
set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SIDELOAD_DIR="$HOME/.runelite/sideloaded-plugins"
LOG="$PROJECT_ROOT/build/clean-build.log"
LOCK="$PROJECT_ROOT/.gradle/clean-build.lock"

mkdir -p "$PROJECT_ROOT/.gradle" "$PROJECT_ROOT/build" 2>/dev/null || true

# Single-flight lock: if a clean-build is already running, skip (avoids pile-up when
# multiple Stop hooks fire in quick succession). mkdir is atomic.
if ! mkdir "$LOCK" 2>/dev/null; then
	echo "clean-build already running — skipped"
	exit 0
fi
trap 'rmdir "$LOCK" 2>/dev/null || true' EXIT

# 1. Clean leftover side-loaded jars (non-fatal if locked by a running client, or absent).
rm -f "$SIDELOAD_DIR"/pvp-enhancer*.jar 2>/dev/null || true

# 2. Build.
cd "$PROJECT_ROOT" || { echo "clean-build: cannot cd to $PROJECT_ROOT"; exit 2; }

if ./gradlew build --no-daemon --console=plain > "$LOG" 2>&1; then
	echo "OK  clean-build passed ($(date '+%H:%M:%S'))"
	exit 0
else
	echo "FAILED  clean-build ($(date '+%H:%M:%S')) — last 30 lines:"
	tail -n 30 "$LOG"
	exit 2
fi
