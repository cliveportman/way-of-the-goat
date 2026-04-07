# Scores Over Time — Retrospective

**Date:** 2026-04-04
**PRs:** [#53](https://github.com/cliveportman/way-of-the-goat/pull/53) (feature), [#54](https://github.com/cliveportman/way-of-the-goat/pull/54) (review fixes)
**Duration:** ~3 days (2026-04-02 to 2026-04-04)

## What Happened

Built a weekly heatmap screen ("Your scores") showing daily diet quality scores over time, with colour-coded tiles per score tier and scrollable week rows. Tapping a scored tile navigates to that date on the Scores screen. Replaced the old Activity screen and its navigation entry.

PR #53 delivered the full feature: design specs (screen + component), `ScoresOverTimeScreen`, `ScoresOverTimeViewModel`, `ScoreWeekRow`, `WeeklyScoreBuilder` with 8 tests, and navigation wiring. PR #54 addressed 7 of the code review issues — extracting a `ServingsDataSource` interface for testability, adding 6 ViewModel tests, consolidating redundant rebuild logic, and cleaning up minor code quality items.

**Scope changes from plan:** No formal plan existed (no `plan.md`). The design-to-code pipeline went straight from Figma export to spec curation to implementation without a planning phase. Spacing values were adjusted during implementation to improve the look in-app, deliberately deviating from the Figma file.

## What Worked Well

- Implementation produced a fully working feature on the first attempt — the design-to-code pipeline generated correct, buildable Compose code from the curated specs without manual debugging
- Extracting `WeeklyScoreBuilder` into a stateless helper (prompted by the first review) was a clean architectural improvement and made the logic straightforward to test
- The `/app-review` command caught real issues: singleton coupling in the ViewModel, missing tests, `Box` + `clickable` instead of `Surface`, stale route naming — all valid and worth fixing

## What Didn't Work Well

- **Spacing didn't match the Figma file.** The initial implementation had spacing deviations (horizontal padding, row spacing, tile height) that needed correcting. Even after aligning more closely with the spec, the design still needs tweaking now that it's visible in the actual app — the Figma didn't fully translate to a good on-device feel
- **Reviews found issues incrementally.** The second and third `/app-review` runs surfaced problems the first review missed (reverse data-layer dependency on UI types, `!!` without justification, fully qualified type names). Three review rounds for one feature is expensive
- **Reviews flagged deliberate spec deviations as issues.** After improving spacings in-app, the reviews kept highlighting mismatches against the design spec. There was no mechanism to signal "I intentionally changed this" — the reviewer treated every spec deviation as a defect
- **Design spec had internal contradictions.** The screen spec listed `s12` for row spacing in one section and `s4` in another; tile height was noted as both 44dp and 48dp. These ambiguities caused confusion during both implementation and review

## Lessons Learned

- **Significant features need a planning phase.** The design-to-code workflow skipped straight from Figma to implementation. For a feature of this size (new screen, ViewModel, data layer helper, navigation changes, old screen removal), the rubber-duck agent should have been involved to produce a `plan.md` covering architecture decisions, data flow, and scope. The design-to-code workflow should prompt the user to optionally invoke the rubber duck for planning before implementation begins
- **Deliberate spec deviations need a way to be communicated.** When spacings are adjusted in-app, the design spec should be updated to match (or annotated with the deviation), so subsequent reviews don't flag intentional changes as bugs
- **Run a single thorough review rather than multiple passes.** The first review missed issues that a more thorough single pass would have caught, resulting in three review cycles. Consider whether the review prompt or checklist needs strengthening to reduce iterations
- **Design specs should be validated for internal consistency before implementation.** The contradictory spacing/sizing notes caused avoidable confusion

## Follow-up Items

- Design overhaul needed after the next feature is implemented — current spacings and layout need revisiting once seen alongside new content
- `ProgressScreen` technical debt (flagged in review but out of scope): `collectAsState()` should be `collectAsStateWithLifecycle()`, ViewModel passed directly to child composables, hardcoded `.dp` literals and `RoundedCornerShape` values should use design tokens
- Consider updating the `/design-to-code` workflow to optionally invoke the rubber-duck agent for planning on significant features — create a `plan.md` before implementation begins
- Update the `/app-review` workflow to accept annotations or a "known deviations" list so deliberate spec changes aren't re-flagged
