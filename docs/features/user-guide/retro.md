# User Guide Screen — Retrospective

**Date:** 2026-04-06
**PR:** [#58 feat: text changes on docs page](https://github.com/cliveportman/way-of-the-goat/pull/58)
**Duration:** April 4–6 (initial HelpScreen stub was in PR #52; content population and polish in PR #58)

## What Happened

The User Guide screen (internally `HelpScreen`) was initially scaffolded as a stub in PR #52. PR #58 populated it with the full content: all 13 food categories across all scoring profiles, ported from the React Native reference app. The bottom nav label was also renamed from "Help" to "User guide".

The initial implementation required two rounds of post-review fixes before it could merge:

- **Round 1 (85bb65d):** 10 issues — critical crash risk, KMP compilation failure, missing modifier params, missing `@Preview` functions, developer section touch target, `GuideEntry` data class extraction
- **Round 2 (3861e6a):** Remaining issues — regex constant, `const val` string constants, further refinements

The screen is functional but the design is acknowledged as needing improvement, consistent with the current state of other screens.

## What Worked Well

- The content itself was cleanly ported — all 13 categories, across all three scoring profiles, covered in one pass
- Post-review, the structure is solid: `GuideEntry` data class, private helper composables (`SectionHeading`, `SectionSubheading`, `BodyText`, `FoodCategoryGuideEntry`), safe `getCategoryById` usage
- Using the React Native reference app as a content source worked well for fidelity

## What Didn't Work Well

- **Critical issues reached PR review that should not have.** Two in particular stand out:
  - The `@Preview` import used the Android-only `androidx.compose.ui.tooling.preview.Preview` instead of the KMP-compatible `org.jetbrains.compose.ui.tooling.preview.Preview` — this would have caused a compilation failure. Every other screen in `commonMain` uses the correct import; this is a known KMP gotcha that the `jake-wharton` agent should handle reliably.
  - `categoryById` used `.first{}` — a crash if a hardcoded category ID string was ever mistyped or a suite changed. The safe `getCategoryById` API already existed.

- **Routine issues also slipped through:** unused imports (`semantics`, `invisibleToUser`), `private val` string constants that should have been `private const val`, missing `modifier` parameters on composables. All of these are in the `kmp-review-criteria` skill but were not applied before submission.

- **Two review rounds** signal that the initial implementation wasn't held to the expected standard before being submitted. The review process caught what it should, but the pre-review quality bar wasn't met.

- **No automated static analysis** is in place. There is no detekt, ktlint, or spotless configured in the mobile build. Several of the review issues (`const val`, unused imports, nullable safe calls) are the kind of thing a linter catches for free.

## Lessons Learned

- The KMP `@Preview` import is a consistent trip hazard. It should be explicitly called out in `jake-wharton`'s agent prompt as a hard rule, not left to the review criteria skill to catch after the fact.
- Nullable-safe APIs (`getCategoryById` vs `first {}`) should be a first-class instinct, not a code review correction.
- The absence of linting tooling means correctness relies entirely on agent discipline and manual review. Adding detekt or ktlint would make many of these issues non-issues.
- The `kmp-review-criteria` skill contains the right rules — but they're not being applied pre-submission. Worth exploring whether the `jake-wharton` agent should do a self-review pass before returning.

## Follow-up Items

- **Code quality / linting:** Introduce static analysis (detekt and/or ktlint) to the mobile build to automate the routine checks — `const val`, unused imports, safe nullable calls, modifier parameter presence. Track in `docs/features/`.
- **Agent quality:** Review whether `jake-wharton`'s prompt should include a mandatory self-review step against `kmp-review-criteria` before the output is considered done.
- **Design pass:** The User Guide screen needs a proper design treatment. No spec exists yet — this should go through the design-to-code pipeline when prioritised.
