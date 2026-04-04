# Runs Over Time — Retrospective

**Date:** 2026-04-04
**PR:** https://github.com/cliveportman/way-of-the-goat/pull/56
**Duration:** ~1 day (first commit to merge, same day)

## What Happened

Replaced the legacy Progress screen (third bottom-nav tab) with a new Runs Over Time screen showing daily endurance activity distances in a scrollable weekly grid. All 7 phases from the plan were delivered: data models, `WeeklyActivityBuilder`, ViewModel, `RunWeekRow` composable, `RunsOverTimeScreen`, navigation wiring (including deletion of `ProgressScreen`/`ProgressViewModel`), and tests.

**Scope changes from plan:**
- `BlankTile` and `dayNameForIndex` were extracted to a shared `WeekRowShared.kt` — the plan mentioned this as optional but it was flagged in code review and addressed.
- `formatDistance` was changed from truncation to rounding (`roundToInt`) after review — the plan noted reusing the original `ProgressScreen` behaviour, but the review correctly identified truncation as misleading for accumulated totals.
- An `ActivityDataSource` interface was introduced for testability (the plan flagged this as a risk and recommended it).

## What Worked Well

- Implementation was very smooth — mirroring the existing `ScoresOverTime` pattern meant most structural decisions were already made.
- The plan's phase-by-phase structure mapped cleanly to the implementation order with no backtracking.
- Code review caught real issues (wrong color tokens, missing tests, truncation vs rounding) and the fix cycle was quick — all 5 issues from the first review resolved in one commit.
- Test coverage was comprehensive from the start: ViewModel tests with Turbine, builder unit tests, and dedicated `FormatDistanceTest` with 6 cases.

## What Didn't Work Well

- **Design-to-device colour fidelity:** `MaterialTheme.colorScheme.surface` was used instead of `MaterialTheme.goatColors.surface` for tile text, dots, and background. The values happen to resolve identically right now, but it's the wrong colour system. This is a recurring theme — the Figma designs don't make it obvious which Compose colour system to target.
- **Spec parenthetical values were wrong:** The component spec documented `GoatSizing.Touch.default (44dp)` when the actual token is `48dp`. The implementation used the token name correctly, but the misleading parenthetical could confuse future readers. Same typo existed in the score-week-row spec.

## Lessons Learned

- **The colour token mapping in `.claude/skills/design-specs/SKILL.md` is the root cause of the `colorScheme` vs `goatColors` issue.** Lines 290-293 map `surface`/`onSurface`/`surfaceVariant`/`onSurfaceVariant` to `MaterialTheme.colorScheme.*` instead of `MaterialTheme.goatColors.*`. The same incorrect example appears in `.claude/skills/kmp-conventions/SKILL.md` line 192. This propagates through the entire pipeline: skill → spec → plan → code. Fixing these two skill files will prevent the issue recurring across all future features.
- **Figma auto-layout spacing needs more attention.** The design doesn't communicate spacing intent clearly enough, leading to spec values that need tweaking on-device. Investing time in Figma auto-layout setup will reduce spec-to-implementation friction.

## Follow-up Items

- Fix `.claude/skills/design-specs/SKILL.md` lines 290-293: change `surface`/`onSurface`/`surfaceVariant`/`onSurfaceVariant` mappings from `MaterialTheme.colorScheme.*` to `MaterialTheme.goatColors.*`
- Fix `.claude/skills/kmp-conventions/SKILL.md` line 192: change `MaterialTheme.colorScheme.surface` to `MaterialTheme.goatColors.surface`
- Fix `design-specs/components/score-week-row.md` line 33: `GoatSizing.Touch.default (44dp)` → `(48dp)` — same typo that was already fixed in `run-week-row.md`
- Improve Figma auto-layout spacing for future design specs
