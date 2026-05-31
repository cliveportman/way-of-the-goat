# PR Create Command

Create a Pull Request from the current branch.

This command has two modes:

- **Feature mode** (default) — a normal PR from the current branch into `main` (or another target).
- **Release mode** — a release PR merging `main` into `prod` to ship to production. Triggered when `$ARGUMENTS` contains `--release`, or when the resolved target branch is `prod`. See [Release PRs](#release-prs) below.

**Target branch**: `$ARGUMENTS` (defaults to `main`; release mode defaults to `prod`).

## Instructions

1. **Determine mode and target branch**:
   - If `$ARGUMENTS` contains `--release` (or the resolved target branch is `prod`), switch to **Release mode** and follow the [Release PRs](#release-prs) section instead of steps 2–8. Release mode targets `prod` by default.
   - Otherwise this is a **Feature mode** PR: if a target branch is given in `$ARGUMENTS`, use it; otherwise default to `main`.

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
   | `website/` | `website` |
   | `design-specs/` only | `spec` |
   | `design-tokens/` only | `tokens` |
   | `api/` only | `api` |
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
   - `🐏 website: add VO2 max calculators` *(no ticket)*

   **Body format**:
   ```markdown
   ## Summary
   - [1–3 bullet points describing what changed and why]

   ## Test plan
   - [ ] [Testing step 1]
   - [ ] [Testing step 2]
   ```

   The test plan should be specific.

   For `mobile` changes, include:
   - Build verification (`./gradlew :composeApp:assembleDebug`)
   - Test run (`./gradlew :composeApp:jvmTest`)
   - Any manual steps needed to verify the feature or fix on device/simulator

   For `website` changes, include:
   - Confirm the Vercel preview deploy builds and renders
   - Exercise any affected routes (e.g. `/vo2/` and each `/vo2/<slug>/` implementation)
   - For WASM/asset changes, confirm `.wasm` is served as `application/wasm` and the page computes a result
   - Check the browser console for CSP violations (blocked scripts/fonts/styles)

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

## Release PRs

Release mode ships everything currently on `main` to production by opening a PR from `main` into `prod` (Vercel deploys from `prod`). Run it from an up-to-date local `main`. Follow these steps **instead of** steps 2–8 above.

### R1. Pre-release safety checks

Run these first and **STOP if any fails** — report the problem to the user and ask how to proceed rather than opening the PR:

- `git fetch origin` — get the latest refs
- Working tree is clean: `git status --porcelain` is empty (ignore known-local untracked files such as `*.local.json` and `.claude/worktrees/`)
- Local `main` is level with the remote: `git rev-parse main` equals `git rev-parse origin/main` (if behind, ask the user to fast-forward first)
- There is something to release: `git log origin/prod..origin/main --oneline` is non-empty
- Scope is intended: show `git log origin/prod..origin/main --oneline` and confirm with the user that this is the full set of changes to ship
- If CI is configured, the latest run on `main` is green: `gh run list --branch main --limit 1`

### R2. Compute the next version

- Find the latest tag: `git describe --tags --abbrev=0` (e.g. `v1.0.1`)
- List the commits since it: `git log [latest-tag]..main --oneline`
- Pick the bump using semver:
  - **Major** (`v2.0.0`) — breaking changes; only when the user explicitly says so
  - **Minor** (`v1.1.0`) — there are any `feat:` commits since the last tag
  - **Patch** (`v1.0.2`) — only `fix:` / `chore:` / `docs:` / `style:` commits
- State the proposed version and your reasoning, and let the user override before continuing.

### R3. Aggregate release notes

Categorise every commit in `[latest-tag]..main` by its semantic prefix into user-facing notes. Describe the user-visible change, not the files. Drop merge commits and tooling chores unless they matter:

```markdown
## Release vX.Y.Z

### Features
- [from `feat:` commits]

### Fixes
- [from `fix:` commits]

### Other
- [refactor/docs/chore worth mentioning]
```

### R4. Create the release PR

- Push `main` if the remote is behind: `git push origin main`
- Title: `[emoji] release: vX.Y.Z` (e.g. `🐏 release: v1.1.0`)
- Base `prod`, head `main`, created as a draft:

```bash
gh pr create --draft --base prod --head main --title "[emoji] release: vX.Y.Z" --body "$(cat <<'EOF'
## Release vX.Y.Z

### Features
- [bullets]

### Fixes
- [bullets]

## Tag plan
After this PR merges, tag the release from an updated `prod`: run `git checkout prod && git pull`, then `git tag vX.Y.Z`, then `git push origin vX.Y.Z`.

## Deploy / verify checklist
- [ ] Vercel production deploy from `prod` succeeds
- [ ] Production URL loads
- [ ] `/vo2/` and each `/vo2/<slug>/` route renders and computes a result
- [ ] `.wasm` assets are served with `Content-Type: application/wasm`
- [ ] No CSP violations in the browser console
- [ ] Trailing-slash redirects resolve correctly
EOF
)"
```

### R5. After creating

- Return the PR URL.
- Remind the user the tag is created **after** merge (per the tag plan in the PR body); offer to run those tag commands once `prod` is updated.
- Do **not** create or push the tag before the PR is merged.

## Notes

- Analyse ALL commits in the branch, not just the latest one
- The summary should reflect the overall purpose, not a list of files changed
- Create as a draft — the author promotes it when ready for review
- If the branch touches both `design-specs/` and `mobile/`, label it `mobile` — the spec change is context for the implementation
- In release mode, never bypass the pre-release safety checks; a failed check means stop and ask
