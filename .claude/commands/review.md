# Smart Review Command

Reviews changed code by auto-routing to the right agent(s) based on which files changed.

> **Note:** Run this command in a fresh context (`/clear` first). Reviewers are designed to be objective; prior conversation context may anchor the review.

---

## Arguments

| Form | Behaviour |
|------|-----------|
| `/review` | Auto-detect changed files vs. base branch; run relevant reviewers |
| `/review <pr-number>` | Fetch PR from GitHub; auto-route based on changed files |
| `/review <pr-number> --recheck` | Re-review mode for mobile domain; full review for all other domains |
| `/review --audit` | Full audit of entire `.claude/` directory (delegates to `claude-reviewer`) |
| `/review --file <path>` | Review a single `.claude/` file (delegates to `claude-reviewer`) |

---

## Step 1: Parse Arguments

Parse `$ARGUMENTS`:

- If `--audit` is present → jump to [Audit Mode](#audit-mode)
- If `--file <path>` is present → jump to [Single File Mode](#single-file-mode)
- If a number is present → it is `<pr-number>`; note whether `--recheck` is also present
- Otherwise → no PR number; use local git diff

---

## Step 2: Determine Changed Files

### With a PR number

Use the GitHub MCP server:

1. Call `mcp__github__get_pull_request` with the PR number to get the branch name and PR metadata
2. Call `mcp__github__list_pull_request_files` to get the list of changed files

### Without a PR number (working tree / current branch)

Run:

```bash
base=$(git merge-base HEAD $(git symbolic-ref refs/remotes/origin/HEAD 2>/dev/null | sed 's|refs/remotes/origin/||') 2>/dev/null || git merge-base HEAD main 2>/dev/null || echo "HEAD~1")
git diff "$base"...HEAD --name-only
```

---

## Step 3: Route to Agents

Evaluate the changed file list against these rules. A single run can match multiple domains — run all that match, sequentially.

| File pattern | Domain | Agent |
|---|---|---|
| `mobile/**/*.kt`, `mobile/**/*.kts` | Mobile (KMP) | `nick-butcher` |
| `website/**/*.html`, `website/**/*.css`, `website/**/*.js` | Web | `addy-osmani` |
| `website/**/*.rs`, `website/**/Cargo.toml` | Rust/WASM | `steve-klabnik` |
| `.claude/**` | Config | `claude-reviewer` |

If no files match any known domain, state that and stop.

---

## Step 4: Run Reviews

### Mobile domain — nick-butcher

#### Worktree Setup (PR review only)

If a PR number was given:

1. Run `git fetch origin <branch>` to get the latest remote state
2. Call `EnterWorktree` (name: `review-<pr-number>-mobile`) — this switches into an isolated worktree
3. Inside the worktree, run `git switch --detach origin/<branch>` to switch to the PR branch
4. From the `mobile/` directory, run `./gradlew detekt`. Note any failures — pass them to Nick Butcher as pre-identified Critical issues.
5. Dispatch the `nick-butcher` agent (see Agent Dispatch below)
6. After the agent completes, call `ExitWorktree` with `action: "remove"`

If no PR number was given, dispatch `nick-butcher` in the current working directory (no worktree).

#### Re-review Mode (`--recheck`)

When `--recheck` is passed for the mobile domain:

1. Fetch all comments: `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`
2. Find the most recent comment whose body starts with "Automated code review" (case-insensitive). Record its timestamp.
3. Determine which `mobile/` files were modified **after** that timestamp.
4. Run the full checklist against those changed files only.
5. Re-check any Critical Issues or Suggestions from the previous review — even if the file was not re-touched.
6. Do **not** raise new issues in files unchanged since the last review. If you notice something in an untouched file, note it under `#### Deferred (untouched files)` — informational only, not numbered.
7. Never re-raise a Nit-pick from a previous review.

#### Agent Dispatch — nick-butcher

Instruct `nick-butcher` to consult:

- `.claude/skills/kmp-review-criteria/SKILL.md`
- `.claude/skills/kmp-conventions/SKILL.md`
- `mobile/CLAUDE.md`

Pass any detekt violations as pre-identified Critical issues.

#### Posting the Mobile Review

Use `get_me` GitHub MCP tool to get your authenticated username. Post the review as a comment on the PR titled "Automated code review by [username]".

#### Previous Review Follow-up (PR review only)

After drafting the review (not before — to avoid anchoring bias):

1. Fetch all comments: `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`
2. Find the most recent comment starting with "Automated code review"
3. If none exists, post as-is and skip this section
4. If one exists, extract all numbered Criticals and Suggestions (not Nit-picks) from it and assess each as: **Resolved**, **Unresolved**, or **No longer applicable**
5. Append the follow-up section before posting:

```
---

### Follow-up from previous review

**Resolved:**
- [number]. [brief description]

**Unresolved:**
- [number]. [brief description]

**No longer applicable:**
- [number]. [brief description — reason]
```

Omit any sub-heading with no items.

---

### Web domain — addy-osmani

#### Worktree Setup (PR review only)

If a PR number was given:

1. Run `git fetch origin <branch>`
2. Call `EnterWorktree` (name: `review-<pr-number>-web`)
3. Inside the worktree, run `git switch --detach origin/<branch>`
4. Dispatch the `addy-osmani` agent
5. After the agent completes, call `ExitWorktree` with `action: "remove"`

If no PR number, dispatch `addy-osmani` in the current working directory.

#### Re-review Mode (`--recheck`)

When `--recheck` is passed, run a full review regardless — there is no scoping to recently-changed files for the web domain.

#### Agent Dispatch — addy-osmani

Instruct `addy-osmani` to:

- Read `.claude/skills/web-review-criteria/SKILL.md` before reviewing
- Focus on files under `website/` that appear in the changed file list
- Use the output format defined in its agent file

The web review is **post-and-proceed**: collect the agent's output and include it in the combined Step 5 comment. There is no interactive fix loop — the user applies fixes and runs `/review --recheck` for a follow-up.

#### Previous Review Follow-up (PR review only)

After collecting the agent's output (not before — to avoid anchoring bias):

1. Fetch all comments: `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`
2. Find the most recent comment starting with "Automated code review"
3. If none exists, skip this section
4. If one exists, extract all numbered Criticals and Suggestions (not Nit-picks) from the web section of that comment and assess each as: **Resolved**, **Unresolved**, or **No longer applicable**
5. Append the follow-up section to the web review output before including it in the combined Step 5 comment. Use the same template as the mobile domain.

---

### Rust/WASM domain — steve-klabnik

#### Worktree Setup (PR review only)

If a PR number was given:

1. Run `git fetch origin <branch>`
2. Call `EnterWorktree` (name: `review-<pr-number>-rust`)
3. Inside the worktree, run `git switch --detach origin/<branch>`
4. Dispatch the `steve-klabnik` agent
5. After the agent completes, call `ExitWorktree` with `action: "remove"`

If no PR number, dispatch `steve-klabnik` in the current working directory.

#### Re-review Mode (`--recheck`)

When `--recheck` is passed, run a full review regardless — there is no scoping to recently-changed files for the Rust/WASM domain.

#### Agent Dispatch — steve-klabnik

Instruct `steve-klabnik` to focus on `website/**/*.rs` and `website/**/Cargo.toml` files that appear in the changed file list.

The Rust/WASM review is **post-and-proceed**: collect the agent's output and include it in the combined Step 5 comment. There is no interactive fix loop.

#### Previous Review Follow-up (PR review only)

After collecting the agent's output (not before — to avoid anchoring bias):

1. Fetch all comments: `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`
2. Find the most recent comment starting with "Automated code review"
3. If none exists, skip this section
4. If one exists, extract all numbered Criticals and Suggestions (not Nit-picks) from the Rust/WASM section of that comment and assess each as: **Resolved**, **Unresolved**, or **No longer applicable**
5. Append the follow-up section to the Rust/WASM review output before including it in the combined Step 5 comment. Use the same template as the mobile domain.

---

### Config domain — claude-reviewer

No worktree needed. The `claude-reviewer` agent reads local files directly.

Dispatch `claude-reviewer` with the list of changed `.claude/` files from Step 2.

After receiving findings, **stop and wait for user instructions**. Do not automatically apply fixes. Accept instructions such as:

- "Fix issue 2" — apply a specific fix
- "Fix all critical" — apply all critical issues
- "Tell me more about suggestion 3" — explain in detail
- "Looks good, nothing to change" — close out

---

## Step 5: Output

### Posting the Combined Review (PR review only)

After collecting all domain reviews, post the combined output as a single PR comment:

1. Run `gh api user --jq '.login'` to get your authenticated username
2. Post using `gh pr comment <pr-number> --body "..."` with the opening line "Automated code review by [username]"

### Output Format

If multiple domains matched, label each section clearly:

```
## Review

### Mobile (nick-butcher)
[review output]

---

### Web (addy-osmani)
[review output]

---

### Rust/WASM (steve-klabnik)
[review output]

---

### .claude/ Config (claude-reviewer)
[review output]
```

---

## Audit Mode

When `--audit` is passed:

Delegate to `claude-reviewer` with instruction to review all files under `.claude/` (use Glob). Present findings, then stop and wait for user instructions (same interactive flow as the Config domain above).

---

## Single File Mode

When `--file <path>` is passed:

Delegate to `claude-reviewer` with instruction to review that specific file. Present findings, then stop and wait.
