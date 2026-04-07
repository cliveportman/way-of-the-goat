#!/bin/bash
input=$(cat)
log="/Users/clive/repos/sides/way-of-the-goat/.claude/activity.log"
ts=$(date '+%Y-%m-%d %H:%M:%S')
cwd=$(echo "$input" | jq -r '.cwd // empty' 2>/dev/null)
[ -z "$cwd" ] && cwd="/Users/clive/repos/sides/way-of-the-goat"
branch=$(git -C "$cwd" branch --show-current 2>/dev/null | tr -d '\r\n')
[ -z "$branch" ] && branch="?"

# Try both wrapped and flat JSON structures, strip any stray \r
agent=$(echo "$input" | jq -r '
  (.tool_input.subagent_type // .subagent_type // "unknown") | gsub("\r"; "")
' 2>/dev/null | tr -d '\r')
[ -z "$agent" ] && agent="unknown"

printf '%s [%s] 🤖 AGENT  %s\n' "$ts" "$branch" "$agent" >> "$log"
