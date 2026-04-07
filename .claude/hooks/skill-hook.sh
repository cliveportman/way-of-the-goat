#!/bin/bash
input=$(cat)
log="/Users/clive/repos/sides/way-of-the-goat/.claude/activity.log"
ts=$(date '+%Y-%m-%d %H:%M:%S')
cwd=$(echo "$input" | jq -r '.cwd // empty' 2>/dev/null)
[ -z "$cwd" ] && cwd="/Users/clive/repos/sides/way-of-the-goat"
branch=$(git -C "$cwd" branch --show-current 2>/dev/null | tr -d '\r\n')
[ -z "$branch" ] && branch="?"

skill=$(echo "$input" | jq -r '
  (.tool_input.skill // .skill // "unknown") | gsub("\r"; "")
' 2>/dev/null | tr -d '\r')
[ -z "$skill" ] && skill="unknown"

printf '%s [%s] 🎯 SKILL  %s\n' "$ts" "$branch" "$skill" >> "$log"
