#!/usr/bin/env bash
set -uo pipefail
source "$(dirname "$0")/env.sh"

fail=0
check() {
  local label="$1"; shift
  if "$@" >/dev/null 2>&1; then echo "ok    $label"; else echo "FAIL  $label"; fail=1; fi
}

check "apikey.properties present" test -s "$REPO_ROOT/core-network/apikey.properties"
check "$SERIAL is attached" bash -c "'$ADB' devices | grep -q '^$SERIAL[[:space:]]*device'"
check "$SERIAL is AVD $AVD_NAME" bash -c "[ \"\$('$ADB' -s '$SERIAL' emu avd name | head -1 | tr -d '\r')\" = '$AVD_NAME' ]"
check "verify-prose started this instance" test -f "$MARKER"
check "API level <= 35" bash -c "[ \"\$('$ADB' -s '$SERIAL' shell getprop ro.build.version.sdk | tr -d '\r')\" -le 35 ]"
check "$PACKAGE installed" bash -c "'$ADB' -s '$SERIAL' shell pm list packages | grep -q 'package:$PACKAGE\$'"
check "installed build is not older than the built APK" bash -c "
  installed=\$('$ADB' -s '$SERIAL' shell dumpsys package '$PACKAGE' | sed -n 's/.*lastUpdateTime=//p' | head -1 | tr -d '\r')
  installed_epoch=\$(date -j -f '%Y-%m-%d %H:%M:%S' \"\$installed\" +%s 2>/dev/null || date -d \"\$installed\" +%s)
  apk_epoch=\$(stat -f %m '$APK' 2>/dev/null || stat -c %Y '$APK')
  [ \"\$installed_epoch\" -ge \"\$apk_epoch\" ]"
check "device can reach the Books API" bash -c "'$ADB' -s '$SERIAL' shell ping -c 1 -W 3 www.googleapis.com"
check "app is in the foreground" bash -c "'$ADB' -s '$SERIAL' shell dumpsys activity activities | grep -q 'topResumedActivity.*$PACKAGE'"

if [ "$fail" -eq 0 ]; then echo "doctor: instance is worth driving"; else echo "doctor: fix the FAIL lines first (re-run scripts/launch.sh)"; fi
exit "$fail"
