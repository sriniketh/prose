#!/usr/bin/env bash
# Stop hook: reminds the agent to update docs/*.md when structural files
# changed without a docs update, per the rules in AGENTS.md.
set -uo pipefail

cd "${CLAUDE_PROJECT_DIR:-$(pwd)}" 2>/dev/null || exit 0

git rev-parse --git-dir >/dev/null 2>&1 || exit 0

BASE_REF=""
for ref in main origin/main; do
  if git rev-parse --verify "$ref" >/dev/null 2>&1; then
    BASE_REF="$ref"
    break
  fi
done
[ -n "$BASE_REF" ] || exit 0

STATE_FILE=".claude/.docs-reminder-state"

MERGE_BASE=$(git merge-base "$BASE_REF" HEAD 2>/dev/null || echo "$BASE_REF")

COMMITTED=$(git diff --name-only "$MERGE_BASE"...HEAD 2>/dev/null || true)
UNCOMMITTED=$(git status --porcelain --untracked-files=all 2>/dev/null | awk '{print $2}')
CHANGED=$(printf '%s\n%s\n' "$COMMITTED" "$UNCOMMITTED" | sort -u | grep -v '^$' || true)

if [ -z "$CHANGED" ]; then
  rm -f "$STATE_FILE"
  exit 0
fi

STRUCTURAL=$(printf '%s\n' "$CHANGED" | grep -E \
  -e '(^|/)build\.gradle\.kts$' \
  -e '^settings\.gradle\.kts$' \
  -e '^build-logic/' \
  -e '(^|/)(di|dagger)/.*\.kt$' \
  -e '^app/src/main/java/com/sriniketh/prose/Navigation\.kt$' \
  || true)

if [ -z "$STRUCTURAL" ]; then
  rm -f "$STATE_FILE"
  exit 0
fi

DOCS=$(printf '%s\n' "$CHANGED" | grep -E '^docs/.*\.md$' || true)

if [ -n "$DOCS" ]; then
  rm -f "$STATE_FILE"
  exit 0
fi

HASH=$(printf '%s' "$STRUCTURAL" | shasum | awk '{print $1}')

mkdir -p .claude
if [ -f "$STATE_FILE" ] && [ "$(cat "$STATE_FILE")" = "$HASH" ]; then
  exit 0
fi
echo "$HASH" > "$STATE_FILE"

REASON="AGENTS.md requires updating docs/*.md in the same change set when structural files change (Gradle module files, Hilt DI wiring, Navigation.kt, or convention plugins under build-logic/). These changed files look structural, but no docs/*.md file was touched:
$STRUCTURAL

Update the relevant file under docs/ (architecture.md, modules.md, convention-plugins.md, flows.md, or the fast facts in README.md — see AGENTS.md for which applies) before finishing, or proceed if you've judged none of the docs rules apply to this change."

jq -n --arg reason "$REASON" '{
  decision: "block",
  reason: $reason,
  hookSpecificOutput: {
    hookEventName: "Stop",
    additionalContext: $reason
  }
}'
