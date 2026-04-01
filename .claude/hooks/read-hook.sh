#!/bin/bash
input=$(cat)
file=$(echo "$input" | jq -r '.tool_input.file_path // ""' 2>/dev/null | tr -d '\r')
log="/Users/clive/repos/sides/way-of-the-goat/.claude/activity.log"
ts=$(date '+%Y-%m-%d %H:%M:%S')
cwd=$(echo "$input" | jq -r '.cwd // empty' 2>/dev/null)
[ -z "$cwd" ] && cwd="/Users/clive/repos/sides/way-of-the-goat"
branch=$(git -C "$cwd" branch --show-current 2>/dev/null | tr -d '\r\n')
[ -z "$branch" ] && branch="?"

if echo "$file" | grep -q '/.claude/skills/'; then
  skill=$(echo "$file" | sed 's|.*/.claude/skills/||;s|/.*||')
  printf '%s [%s] 🎯 SKILL  %s\n' "$ts" "$branch" "$skill" >> "$log"

elif echo "$file" | grep -q '/.claude/commands/'; then
  cmd=$(echo "$file" | sed 's|.*/.claude/commands/||;s|\.md$||')
  printf '%s [%s] 📎 COMMAND  %s\n' "$ts" "$branch" "$cmd" >> "$log"

elif echo "$file" | grep -qE '[^/]+/CLAUDE\.md$' && ! echo "$file" | grep -q '/.claude/'; then
  label=$(echo "$file" | sed 's|.*/way-of-the-goat/||;s|/CLAUDE\.md$||')
  printf '%s [%s] 📋 CLAUDE.md  %s\n' "$ts" "$branch" "$label" >> "$log"
fi
