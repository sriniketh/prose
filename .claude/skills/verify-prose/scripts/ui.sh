#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/env.sh"

usage() {
  cat >&2 <<'EOF'
usage: ui.sh <command> [args]
  dump                    print every visible text / content-desc with its centre
  has <needle>            exit 0 if a node's text or content-desc equals (else contains) needle
  wait <needle> [secs]    poll until has <needle> succeeds (default 15s)
  gone <needle> [secs]    poll until has <needle> fails (default 15s)
  tap <needle>            tap the centre of the matching node
  tap-xy <x> <y>          tap raw device pixels
  type <text>             type into the focused field (spaces are escaped)
  key <keycode>           send a key event, e.g. KEYCODE_BACK
  back                    press back
  hide-keyboard           close the soft keyboard if it is showing (never pops the screen)
  shot <name>             save $EVIDENCE_DIR/<name>.png
  tree <name>             save the layout JSON to $EVIDENCE_DIR/<name>.json
EOF
  exit 2
}

layout() { android layout --device "$SERIAL" -p 2>/dev/null | sed -n '/^\[/,$p'; }

center_of() {
  local needle="$1"
  layout | jq -r --arg n "$needle" '
    [.. | objects | select(has("center"))] as $nodes
    | ([$nodes[] | select((.text // "") == $n or (.["content-desc"] // "") == $n)] +
       [$nodes[] | select(((.text // "") | contains($n)) or ((.["content-desc"] // "") | contains($n)))])
    | first | .center // empty' | tr -d '[]' | tr ',' ' '
}

cmd="${1:-}"; shift || true
case "$cmd" in
  dump)
    layout | jq -r '.. | objects | select(has("center")) | select((.text // "") != "" or (.["content-desc"] // "") != "") | "\(.center)\t\(.text // "")\t\(.["content-desc"] // "")"'
    ;;
  has)
    [ -n "$(center_of "${1:?needle}")" ]
    ;;
  wait|gone)
    needle="${1:?needle}"; limit="${2:-15}"; deadline=$((SECONDS + limit))
    while [ "$SECONDS" -lt "$deadline" ]; do
      found=0; [ -n "$(center_of "$needle")" ] && found=1
      if { [ "$cmd" = wait ] && [ "$found" = 1 ]; } || { [ "$cmd" = gone ] && [ "$found" = 0 ]; }; then exit 0; fi
      sleep 1
    done
    echo "timed out after ${limit}s: $cmd '$needle'" >&2
    exit 1
    ;;
  tap)
    needle="${1:?needle}"; xy="$(center_of "$needle")"
    [ -n "$xy" ] || { echo "no node matches '$needle'" >&2; exit 1; }
    adb_s shell input tap $xy
    ;;
  tap-xy)
    adb_s shell input tap "${1:?x}" "${2:?y}"
    ;;
  type)
    adb_s shell input text "$(printf '%s' "${1:?text}" | sed 's/ /%s/g')"
    ;;
  key)
    adb_s shell input keyevent "${1:?keycode}"
    ;;
  back)
    adb_s shell input keyevent KEYCODE_BACK
    ;;
  hide-keyboard)
    if adb_s shell dumpsys input_method | grep -q 'mInputShown=true'; then
      adb_s shell input keyevent KEYCODE_BACK
      sleep 1
    fi
    ;;
  shot)
    mkdir -p "$EVIDENCE_DIR"
    adb_s exec-out screencap -p > "$EVIDENCE_DIR/${1:?name}.png"
    echo "$EVIDENCE_DIR/$1.png"
    ;;
  tree)
    mkdir -p "$EVIDENCE_DIR"
    layout > "$EVIDENCE_DIR/${1:?name}.json"
    echo "$EVIDENCE_DIR/$1.json"
    ;;
  *)
    usage
    ;;
esac
