#!/usr/bin/env bash
set -uo pipefail
source "$(dirname "$0")/env.sh"

if [ -f "$MARKER" ]; then
  adb_s shell am force-stop "$PACKAGE" >/dev/null 2>&1
  adb_s shell pm clear "$PACKAGE" >/dev/null 2>&1
  adb_s emu kill >/dev/null 2>&1

  if [ -f "$STATE_DIR/emulator.pid" ]; then
    pid="$(cat "$STATE_DIR/emulator.pid")"
    for _ in 1 2 3 4 5 6 7 8 9 10; do
      kill -0 "$pid" 2>/dev/null || break
      sleep 1
    done
    if kill -0 "$pid" 2>/dev/null; then
      echo "emulator pid $pid ignored 'emu kill'; sending SIGTERM to that pid only" >&2
      kill "$pid"
    fi
  fi
  rm -rf "$STATE_DIR"
  echo "stopped the emulator this run started on $SERIAL and removed $STATE_DIR"
else
  echo "no marker at $MARKER: this run did not start an emulator, nothing to stop"
fi

echo "evidence kept at $EVIDENCE_DIR"
ls -1 "$EVIDENCE_DIR" 2>/dev/null || true
