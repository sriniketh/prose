#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/env.sh"

mkdir -p "$EVIDENCE_DIR" "$STATE_DIR"

if [ ! -s "$REPO_ROOT/core-network/apikey.properties" ]; then
  echo "missing core-network/apikey.properties: copy it from the main checkout or create it (see AGENTS.md)" >&2
  exit 1
fi

running_avd="$("$ADB" -s "$SERIAL" emu avd name 2>/dev/null | head -1 | tr -d '\r' || true)"

if [ -n "$running_avd" ]; then
  if [ ! -f "$MARKER" ]; then
    echo "$SERIAL is already running ($running_avd) and was not started by verify-prose; refusing to drive it. Set PROSE_VERIFY_PORT to another even port." >&2
    exit 1
  fi
  echo "reusing $SERIAL started by an earlier verify-prose run"
else
  other="$("$ADB" devices | awk -v want="$AVD_NAME" '/^emulator-/ {print $1}' | while read -r s; do
    n="$("$ADB" -s "$s" emu avd name 2>/dev/null | head -1 | tr -d '\r')"
    [ "$n" = "$AVD_NAME" ] && echo "$s"
  done)"
  if [ -n "$other" ]; then
    echo "AVD $AVD_NAME is already running as $other; a -read-only second instance would conflict. Stop it or set PROSE_VERIFY_AVD to another AVD." >&2
    exit 1
  fi
  nohup "$EMULATOR" -avd "$AVD_NAME" -read-only -port "$PORT" -no-window -no-snapshot -no-audio -no-boot-anim -gpu swiftshader_indirect \
    > "$STATE_DIR/emulator.log" 2>&1 &
  echo "$!" > "$STATE_DIR/emulator.pid"
  touch "$MARKER"
  "$ADB" -s "$SERIAL" wait-for-device
fi

until [ "$(adb_s shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 2; done

api="$(adb_s shell getprop ro.build.version.sdk | tr -d '\r')"
if [ "$api" -gt 35 ]; then
  echo "device is API $api; the app's Espresso tooling and this skill assume API <= 35. Use the Pixel_8 AVD." >&2
  exit 1
fi

(cd "$REPO_ROOT" && ./gradlew :app:assembleDebug -q)
adb_s install -r "$APK"
adb_s shell pm clear "$PACKAGE" >/dev/null
adb_s shell settings put global window_animation_scale 0
adb_s shell settings put global transition_animation_scale 0
adb_s shell settings put global animator_duration_scale 0
adb_s shell am start -W -n "$PACKAGE/.MainActivity" >/dev/null
echo "ready: $SERIAL ($AVD_NAME, API $api) running a fresh install with an empty bookshelf"
