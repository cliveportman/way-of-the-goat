# Retrospective Command

Write a post-merge retrospective for a feature or issue.

**Arguments**: `$ARGUMENTS` — the feature or issue name (kebab-case, matching the folder in `docs/features/` or `docs/issues/`)

## Instructions

1. **Determine the document type and path**:

   - If `docs/features/$ARGUMENTS/plan.md` exists → this is a feature retro
     - Output path: `docs/features/$ARGUMENTS/retro.md`
   - Else if `docs/issues/$ARGUMENTS/plan.md` or `docs/issues/$ARGUMENTS/fix-plan.md` exists → this is an issue retro
     - Output path: `docs/issues/$ARGUMENTS/retro.md`
   - Else → tell the user no plan was found for `$ARGUMENTS` and list available features/issues under `docs/`

2. **Read the plan**:

   - Read the `plan.md` (or `fix-plan.md`) to understand the intended approach, phases, and tasks

3. **Gather git history** (run in parallel):

   - `git log --oneline --all --grep="$ARGUMENTS"` — commits mentioning this feature/issue
   - `git log --oneline -30` — recent history for context
   - If the user provides a PR number or branch name, also run:
     - `gh pr view [number] --json title,body,mergedAt,additions,deletions,files`
     - `git log main..[branch] --oneline` (if branch is known)

4. **Analyse the PR** (if a PR number is available):

   - `gh pr view [number] --json comments,reviews` — review feedback
   - `gh pr diff [number] --name-only` — files changed

5. **Ask the user** (concisely, in one go):

   - What went well?
   - What didn't go well or took longer than expected?
   - Anything you'd do differently next time?
   - Any follow-up work needed?

   If the user says "skip" or provides no input, write the retro based solely on the plan vs. what was merged.

6. **Write the retrospective** at the output path determined in step 1:

   ```markdown
   # {Feature/Issue Name} — Retrospective

   **Date:** {today's date, YYYY-MM-DD}
   **PR:** {link, if known}
   **Duration:** {rough time from first commit to merge, if determinable}

   ## What Happened

   {Brief narrative: what was built, any scope changes from the plan}

   ## What Worked Well

   - {Things worth repeating}

   ## What Didn't Work Well

   - {Pain points, surprises, things that took longer than expected}

   ## Lessons Learned

   - {Concrete takeaways for future work}

   ## Follow-up Items

   - {Bugs discovered, technical debt introduced, future improvements}
   ```

7. **Return a summary** to the user with the file path and a one-line overview.

## Notes

- Compare the plan against what was actually merged — note any scope changes, dropped tasks, or additions
- Be specific and factual, not generic ("tests were useful" → "Turbine StateFlow tests caught the off-by-one in score calculation before it reached review")
- If there's nothing meaningful for a section, write "None identified" rather than leaving it empty
- Keep it concise — the retro should be useful to skim in 60 seconds
