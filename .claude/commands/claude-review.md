# Review .claude/ Directory Changes

Review changes or additions to the `.claude/` directory using the `claude-reviewer` agent. This command is interactive — findings are presented first, then you decide what (if anything) to fix.

## Arguments

- **No arguments** — review all `.claude/` changes on the current branch vs. its base
- **`--audit`** — full review of the entire `.claude/` directory regardless of git state
- **`--file <path>`** — review a single specific file (e.g. `--file .claude/agents/jake-wharton.md`)

## Instructions

### 1. Determine scope

Parse `$ARGUMENTS`:

- If `--audit` is present: scope is all files under `.claude/` (use Glob)
- If `--file <path>` is present: scope is that single file
- If no arguments: scope is files changed on the current branch

For the default (no arguments) mode, identify changed files:

```bash
# Find the merge-base with main (or kmp if on a feature branch)
base=$(git merge-base HEAD $(git symbolic-ref refs/remotes/origin/HEAD 2>/dev/null | sed 's|refs/remotes/origin/||') 2>/dev/null || git merge-base HEAD main 2>/dev/null || echo "HEAD~1")
git diff "$base"...HEAD --name-only -- .claude/
```

If no `.claude/` files are changed, inform the user and stop.

### 2. Delegate to the claude-reviewer agent

Pass the list of files (or scope description) to the `claude-reviewer` agent for analysis. The agent will:

- Read `claude-review-criteria` skill
- Read each file in scope
- Check CLAUDE.md consistency
- Produce structured findings

> **Note:** Run this command in a fresh context where possible (`/clear` first). The reviewer is designed to be objective; carrying forward prior conversation context may anchor the review.

### 3. Present findings

The agent will return findings in this format:

```
## .claude/ Review

### Critical Issues
### Suggestions
### Nit-picks

---
**Agents:** ...
**Skills:** ...
**Commands:** ...
**Hooks:** ...
**CLAUDE.md consistency:** ...
```

### 4. Stop and wait for instructions

After presenting findings, **stop**. Do not make changes automatically. Wait for the user to respond with instructions such as:

- "Fix issue 2" — apply a specific fix
- "Fix all critical" — apply all critical issues
- "Tell me more about suggestion 3" — explain in detail
- "Looks good, nothing to change" — close out

### 5. Apply requested fixes

When the user specifies fixes:

1. Make the requested changes
2. Report what was changed
3. Ask if they'd like to commit

### 6. Commit (if requested)

If the user wants to commit the `.claude/` changes, suggest they run `/commit`.
