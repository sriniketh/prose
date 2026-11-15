#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/env.sh"

[ $# -ge 1 ] || { echo "usage: db.sh '<sql>'   (queries databases/book-db inside the debug app; SQL goes over stdin so quotes and parentheses survive)" >&2; exit 2; }
printf '%s\n' "$1" | adb_s shell run-as "$PACKAGE" sqlite3 databases/book-db
