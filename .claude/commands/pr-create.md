# PR Create Command

Create a Pull Request from the current branch.

**Target branch**: `$ARGUMENTS` (defaults to `main` if not specified)

## Instructions

1. **Determine target branch**:
   - If `$ARGUMENTS` is provided, use it as the target branch
   - Otherwise, default to `main`

2. **Gather information** by running these commands in parallel:
   - `git status` — current state
   - `git diff [target-branch]...HEAD` — all changes
   - `git log [target-branch]...HEAD --oneline` — all commits
   - `git branch --show-current` — current branch name
   - `git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null` — check if tracking remote

3. **Extract ticket number** from branch name:
   - Look for patterns like `1234-description`, `feature/1234-something`, `cp/1234-something`
   - Extract the numeric portion (e.g., `1234`)
   - If no number is found, ask the user for one or proceed without it

4. **Identify the primary area of change**:

   | Files changed in | Label |
   |---|---|
   | `mobile/` | `mobile` |
   | `design-specs/` only | `spec` |
   | `design-tokens/` only | `tokens` |
   | `.claude/` only | `chore` |
   | Mix of `mobile/` and `design-specs/` | `mobile` |

5. **Push to remote** if the branch is not yet tracking or is ahead:
   - Run `git push -u origin [current-branch]` if needed

6. **Identify contributor emoji**:
   - Run `git config user.name`
   - Look up from this mapping:

     | Git Username | Preferred Emoji |
     |---|---|
     | Clive Portman | 🐏 |

7. **Create the PR** using `gh pr create`:

   **Title format**:
   ```
   [emoji] #[ticket] [label]: [short description]
   ```

   Examples:
   - `🐏 #42 mobile: add food logging screen`
   - `🐏 #43 spec: curate score card component spec`
   - `🐏 mobile: fix crash on empty serving list` *(no ticket)*

   **Body format**:
   ```markdown
   ## Summary
   - [1–3 bullet points describing what changed and why]

   ## Test plan
   - [ ] [Testing step 1]
   - [ ] [Testing step 2]
   ```

   The test plan should be specific. For `mobile` changes, include:
   - Build verification (`./gradlew :composeApp:assembleDebug`)
   - Test run (`./gradlew :composeApp:jvmTest`)
   - Any manual steps needed to verify the feature or fix on device/simulator

   **Execute**:
   ```bash
   gh pr create --draft --base [target-branch] --title "[title]" --body "$(cat <<'EOF'
   ## Summary
   - [bullets]

   ## Test plan
   - [ ] [steps]
   EOF
   )"
   ```

8. **Return the PR URL** to the user.

   If a matching feature or issue exists in `docs/`, remind the user they can run `/retro {name}` after merge to write the retrospective.

## Notes

- Analyse ALL commits in the branch, not just the latest one
- The summary should reflect the overall purpose, not a list of files changed
- Create as a draft — the author promotes it when ready for review
- If the branch touches both `design-specs/` and `mobile/`, label it `mobile` — the spec change is context for the implementation
