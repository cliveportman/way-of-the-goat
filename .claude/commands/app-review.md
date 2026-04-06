Use the github mcp server to fetch PR $ARGUMENTS and perform a thorough **mobile app code review**.

This review covers changes in the Kotlin Multiplatform mobile app:

- `mobile/composeApp/src/commonMain/` — shared KMP code (primary)
- `mobile/composeApp/src/androidMain/` — Android-specific code
- `mobile/composeApp/src/iosMain/` — iOS-specific code
- `mobile/composeApp/src/commonTest/` — shared tests

If the PR contains no files in the `mobile/` directory, state that this command is for the mobile app only.

---

## Review Mode

Determine the mode from `$ARGUMENTS`:

- **`app-review <number>`** — Full review. Review all mobile files in the PR against the full checklist.
- **`app-review <number> --recheck`** — Re-review mode. See [Re-review Mode](#re-review-mode) below.

> **Note:** Always run this command in a **fresh context** (i.e. after `/clear`). The Previous Review Follow-up section handles continuity deliberately; carrying forward prior context risks anchoring the new review.

---

## Worktree Setup (main session)

These steps are performed by the main session before dispatching the Nick Butcher agent:

1. Fetch the PR details from GitHub to get the branch name.
2. Run `git fetch origin <branch>` to ensure the latest remote state is available. Do NOT rely on a local branch which may be out of date.
3. Call the `EnterWorktree` tool (with a name like `app-review-<number>`) — this switches the session into an isolated worktree.
4. Inside the worktree, run `git reset --hard origin/<branch>` to switch to the PR branch. (Safe — the worktree is isolated and will be removed on `ExitWorktree`.)
5. From the `mobile/` directory in the worktree, run `./gradlew detekt`. Note any failures — pass them to Nick Butcher as pre-identified Critical issues.
6. Dispatch the Nick Butcher agent (see Agent Routing below) to perform the review inside the worktree.

---

## Re-review Mode

When `--recheck` is passed:

1. Fetch all comments on the PR using `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`.
2. Find the most recent comment whose body starts with "Automated code review" (case-insensitive). Record its timestamp.
3. Determine which files were modified **after** that timestamp. Only files in the `mobile/` directory count.
4. Run the full checklist, but **only against those changed files**.
5. Re-check any **Critical Issues** or **Suggestions** from the previous review to determine whether they have been resolved — even if the relevant file was not re-touched.
6. **Do not raise new issues in files that were not changed since the last review.** If you notice something in an untouched file, note it under a clearly labelled section: `#### Deferred (untouched files)` — informational only, not numbered.
7. **Never re-raise a Nit-pick** from a previous review. Nit-picks are surfaced once; the author decides; the matter is closed.

---

## Agent Routing

Use the **Nick Butcher** agent for this review. Before reviewing, the agent should consult:

- `.claude/skills/kmp-review-criteria/SKILL.md` — the full review checklist
- `.claude/skills/kmp-conventions/SKILL.md` — architecture, patterns, naming
- `mobile/CLAUDE.md` — project-specific conventions and design token system

If `./gradlew detekt` (step 5 above) produced violations, include them in Nick Butcher's prompt so they are surfaced as Critical issues without requiring the agent to run Bash.

---

# Mobile App Review Checklist

## 1. Architecture (Critical)

This is the most important concern in this review. The app uses MVVM + Repository:

```
@Composable Screen → ViewModel (StateFlow) → Repository → SQLDelight / Ktor
```

**Flag as a critical issue when:**

- Business logic appears inside a `@Composable` function (anything beyond display formatting)
- A `ViewModel` calls SQLDelight queries or Ktor directly, bypassing the Repository layer
- A `Repository` throws exceptions instead of returning `Result<T>`
- `GlobalScope` or bare `CoroutineScope` is used instead of `viewModelScope`
- A `DataManager` (`*DataManager.kt`) is misused for one-off network calls rather than in-memory caching

**Correct pattern:**

```kotlin
// Repository — always Result<T>
suspend fun getServings(date: LocalDate): Result<List<Serving>> = runCatching { ... }

// ViewModel — fold the result
viewModelScope.launch {
    repository.getServings(today).fold(
        onSuccess = { _uiState.value = UiState.Success(it) },
        onFailure = { _errors.emit(it.message ?: "Error") }
    )
}
```

## 2. Previews & Tests (Gate Check)

**For every new `@Composable` function added in this PR**, check:

1. **`@Preview`** — at minimum a default (dark) and a light theme variant, immediately below the composable.
2. **Test** in `commonTest/` — mirroring the source path. ViewModel tests are required for any new `ViewModel`. Pure UI composables are lower priority but should have tests if they contain conditional logic.

Flag missing previews or tests as a **critical issue** unless:
- The composable is a thin screen orchestrator with no logic (just delegates to child composables)
- The composable is a one-line structural wrapper

Preview functions must be `private` and wrapped in `WayOfTheGoatTheme`.

## 3. Design Spec Compliance

Before flagging any design spec deviation as an issue, locate the relevant spec file by searching `design-specs/screens/` and `design-specs/components/` for a filename matching the screen or component under review. If a matching spec exists, check whether it has a `## Deviations` section. This section records **deliberate** differences between the spec and the implementation — values that were adjusted during or after implementation (e.g., spacing tweaked for better on-device feel).

**If the deviation is listed:** do not flag it. It was intentional.
**If no matching spec exists:** skip this check.
**If the deviation is NOT listed:** flag it as a suggestion (not critical), noting the spec value and the implemented value, so the author can confirm whether it's intentional or accidental.

Design spec deviations are **suggestions, not critical issues** — the spec is a starting point, not a contract. Only flag as critical if the deviation causes a functional or accessibility problem (e.g., touch targets below 48dp).

## 4. Design Token Usage (Critical)

**No hardcoded colours, spacing, radius, or type sizes.** All values must come from the design token system.

**Flag as a critical issue when:**

```kotlin
// ❌ Flag these
Color(0xFF9AE600)
Color.Red
16.dp                          // bare magic number
RoundedCornerShape(8.dp)       // use GoatRadius.*
fontSize = 14.sp               // use MaterialTheme.typography.*
```

```kotlin
// ✅ Correct
MaterialTheme.goatColors.scorePlus2
MaterialTheme.colorScheme.surface
GoatSpacing.s16
GoatRadius.md
GoatStroke.emphasis
MaterialTheme.typography.bodyMedium
```

When flagging, reference the appropriate token from `ui/theme/` by name.

## 5. Compose Conventions

- `modifier: Modifier = Modifier` present on all composable functions
- State collected with `collectAsStateWithLifecycle()` — not `collectAsState()`
- ViewModels not passed as parameters to child composables — data and lambdas passed down instead
- `Surface` (or `Card`) used for clickable containers — not `Box`/`Column` with `.clickable {}`
- No `@Composable` lambda capturing state incorrectly (lambdas should read state at call site, not capture a snapshot)
- `Modifier.minimumInteractiveComponentSize()` or `GoatSizing.Touch.default` applied to small interactive elements

## 6. State Management

- `MutableStateFlow` is `private`; `StateFlow` is the public API (`asStateFlow()`)
- `MutableSharedFlow` is `private`; `SharedFlow` is the public API (`asSharedFlow()`)
- One-shot events (errors, navigation triggers) use `SharedFlow` — not `StateFlow`
- Sealed classes used for screen UI states (`Loading`, `Success`, `Error`)
- Value classes (`@JvmInline value class`) used for domain IDs — not raw `Long`/`String`

## 7. Kotlin Quality

- No `!!` (non-null assertion) without a comment explaining why it's safe
- No `var` where `val` would work
- `data class` for value types; `sealed class`/`sealed interface` for closed hierarchies
- Prefer `fold` / `map` / `onSuccess` / `onFailure` on `Result<T>` over manual `isSuccess` checks
- `runCatching { }` at the data layer boundary — not deeper

## 8. KMP Conventions

- `commonMain` has no Android (`android.*`) or iOS (`platform.*`) imports
- Platform differences use `expect`/`actual` — not runtime platform checks
- `kotlinx.datetime` used throughout — not `java.time.*`
- New files placed in `commonMain` unless the code is genuinely platform-specific

## 9. Testing Standards

- `runTest` used for all coroutine tests — not bare `runBlocking`
- Turbine used for `StateFlow` / `SharedFlow` assertions
- Test naming: backtick style — `fun \`given X when Y then Z\``
- Fakes/stubs used for repositories — not real SQLDelight databases in unit tests
- New public functions in the data layer have at least one test covering the happy path

## 10. Accessibility

- Icons that convey meaning have a non-null `contentDescription`
- Decorative icons have `contentDescription = null`
- `Modifier.semantics { }` used on complex interactive components where the default semantics are insufficient
- Touch targets meet `GoatSizing.Touch.default` (48dp) for interactive elements

## 11. Error Handling

- Errors not swallowed silently — `onFailure` is always handled
- User-facing error messages surfaced via `SharedFlow` event — not left as internal `println` or `Log.e`
- No `try/catch(Exception e) { /* ignored */ }` without a comment

---

## Issue Severity Guide

| Tier | Meaning | Merge impact |
|------|---------|--------------|
| **Critical** | Correctness, architectural violations, missing required artefacts (previews/tests), hardcoded tokens, security issues | Must be fixed before merge |
| **Suggestion** | Better approaches exist; meaningful improvement to maintainability, performance, or correctness | Worth fixing; not blocking |
| **Nit-pick** | Style preferences, minor consistency points, trivial improvements | Author decides; never re-raised |

When uncertain between Critical and Suggestion: _would this cause a bug, a crash, or make the codebase meaningfully harder to work with?_ If yes: Critical.

---

## Output Format

Use exactly this structure. No other sections, no preamble:

```
## Code Review for PR [number]

### Critical Issues

[Numbered list. If none, write "None."]

### Suggestions for Improvement

[Numbered list continuing from where critical issues left off. If none, write "None."]

### Nit-picks

[Numbered list continuing from where suggestions left off. If none, write "None."
Nit-picks will not be re-raised in subsequent reviews.]

---

**Architecture:** [single sentence — are the MVVM + Repository layers respected?]
**Design spec compliance:** [single sentence — any deviations from spec, and were they documented?]
**Design tokens:** [single sentence — any hardcoded values?]
**Previews & tests:** [single sentence — are new composables covered?]
**Kotlin quality:** [single sentence assessment]
**Accessibility:** [single sentence assessment]
**KMP conventions:** [single sentence — commonMain clean of platform imports?]

**Rating:** [rating]

[Previous review follow-up section, if applicable]

**Reviewed by:** Nick Butcher
```

The rating scale:
🍋 1 lemon
🍆🍆 2 aubergines
🌽🌽🌽 3 sweetcorn
🍉🍉🍉🍉 4 watermelons
🍏🍏🍏🍏🍏 5 apples
🍇🍇🍇🍇🍇🍇 6 grapes
🍊🍊🍊🍊🍊🍊🍊 7 oranges
🍓🍓🍓🍓🍓🍓🍓🍓 8 strawberries
🍌🍌🍌🍌🍌🍌🍌🍌🍌 9 bananas
🍒🍒🍒🍒🍒🍒🍒🍒🍒🍒 10 cherries
Make it random and nothing to do with the PR's actual quality (we're not judging here)

### Numbering Rules

Number all issues sequentially across all three tiers. Critical issues start at 1. Suggestions continue from where criticals left off. Nit-picks continue from where suggestions left off.

Only use a decimal place to group multiple related items under one topic (e.g., 1.1, 1.2). If a topic has only one point, use the plain number without a decimal.

---

## Previous Review Follow-up

**Important:** Only perform this step _after_ the full review above is drafted. Do not read the previous review before drafting — this avoids anchoring bias.

1. Fetch all comments using `gh api repos/{owner}/{repo}/issues/{pr_number}/comments`
2. Find the most recent comment whose body starts with "Automated code review" (case-insensitive)
3. If **no previous automated review exists**, skip this section entirely and post as-is
4. If a previous review exists:
   - Extract all numbered **Critical Issues** and **Suggestions**. Do **not** extract Nit-picks.
   - For each previously raised issue, determine whether it is:
     - **Resolved** — the code has changed to address it
     - **Unresolved** — the issue still exists
     - **No longer applicable** — the relevant code has been removed or substantially rewritten
   - Append this section before posting:

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

Omit any sub-heading if there are no items in that category.

---

## Posting the Review

Use the `get_me` GitHub MCP tool to retrieve your authenticated GitHub username. Add the review as a comment on the PR, titled "Automated code review by [your username]". Do not add markdown links to any GitHub profiles.

---

## Cleanup (main session)

After the agent completes and the review is posted, call `ExitWorktree` with `action: "remove"` to delete the worktree and return to the original working directory.
