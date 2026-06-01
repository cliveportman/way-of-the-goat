# 001. Move from Kotlin Multiplatform to native Android, scoped to food scoring

**Date:** 2026-05-31
**Status:** Accepted

## Context

The mobile app currently lives in `mobile/` as a Kotlin Multiplatform (KMP) project using Compose Multiplatform, targeting both Android and iOS. It was itself migrated from the React Native v1.4.3 app (kept under `mobile/references/`).

Over time the app has grown beyond food scoring. Alongside the scoring engine it now carries a full Intervals.icu activity integration — API key / OAuth auth (`data/auth`), an HTTP client and API layer (`data/remote`), activity caching (`ActivityDataManager`, `IntervalsRepository`), and a "Runs Over Time" movement-analysis screen. This networking + auth subsystem is the bulk of the app's complexity.

Several forces motivate a change:

- **Complexity to shed.** The KMP multiplatform build (expect/actual, multiplatform Gradle, Compose Multiplatform resources) plus the Compose-Multiplatform-on-iOS story carry ongoing cost and risk that aren't paying for themselves at the current stage.
- **The real product is food scoring.** The endurance/activity features have expanded surface area well beyond the core value, which is scoring food quality against the Racing Weight suites.
- **The scoring core is already portable.** The scoring logic lives in `commonMain` as plain Kotlin (`data/scoring` — `ScoreCalculator`, `SuiteDefinitions`, `ScoringSuite`, and models) with no platform-specific code, so it lifts into a native Android module near-verbatim. Compose Multiplatform UI maps almost one-to-one onto Jetpack Compose.
- **Android already ships.** There is an existing Play Store presence for Android, so an Android-first release path is short.

## Decision

1. **Abandon KMP in favour of a native Android app** written in Kotlin + Jetpack Compose. This is primarily a removal of multiplatform scaffolding rather than a rewrite: the `commonMain` scoring package and Compose UI carry over with minimal change.

2. **Android-first; iOS deferred.** No iOS work in this phase. A future native iOS app would be built separately in Swift/SwiftUI, accepting that the scoring logic would be duplicated there (acceptable given how small and stable the scoring core is).

3. **Drastically reduce scope to food scoring only.**
   - **Keep:** the scoring engine and all suites (Racing Weight, Body composition, …); daily food-serving logging (`ServingsRepository` + local database); the Scores screen as the app's launch surface; Score history (Scores Over Time); Help / user guide; the first-run onboarding/intro flow.
   - **Cut:** the entire Intervals.icu stack (`data/auth`, `data/remote`, `ActivityDataManager`, `IntervalsRepository`, Runs Over Time). The app becomes **fully local and offline — no accounts, no sync, no networking.** Also cut `DesignTokensScreen` (an internal dev tool). ~~Also cut `HomeScreen` (the Scores screen replaces it).~~ — see amendment below.
   - **App shape:** on a normal launch, open straight to the Scores screen; on first launch only, show the branded welcome screen → intro flow → Scores.

> **Amendment (2026-06-01, during Phase 5):** `HomeScreen` is **retained**, not cut. The original cut list mistook it for a redundant landing surface; it is in fact the branded first-run welcome/splash (GoatMoon logo, app title, *Racing Weight* attribution) whose Continue button gates into the intro flow. Returning users still launch straight to Scores, so the "Scores is the launch surface" intent holds. Only the developer-only `DesignTokensScreen` and the activity screens (`RunsOverTimeScreen`, `RunWeekRow`) were dropped.

4. **Keep the KMP app as a frozen reference.** Move the existing `mobile/` KMP project out of the active build (mirroring the `references/` pattern used for RN v1.4.3) so the new native module builds cleanly while the KMP source remains available for porting.

Persistence technology for the native app (keep SQLDelight, which works on Android alone, vs. switch to Room) is **deliberately left open** and will be captured in a separate ADR (002) once the new module is being scaffolded.

## Consequences

**Positive:**
- Removes the multiplatform build complexity and the Compose-Multiplatform-on-iOS risk.
- The scoring core ports near-verbatim; Compose Multiplatform → Jetpack Compose is near-identical, keeping migration effort low.
- A fully local/offline app has no auth, networking, or sync to build or maintain.
- Short path to a published Android release given the existing Play Store presence.
- Much smaller, single-purpose codebase that's easier to reason about and review.

**Negative:**
- A future native iOS app would duplicate the scoring logic in Swift — no shared code across platforms.
- The Intervals.icu activity features are removed entirely (a deliberate loss).
- `.claude/` tooling must be retargeted from KMP to native Android: `nick-butcher`/`jake-wharton` agent criteria, the `kmp-conventions` and `kmp-review-criteria` skills (rename to `android-*`, strip multiplatform checks), and `CLAUDE.md` (already partly stale — root `references/` no longer exists).

**Neutral:**
- Reviewer personas don't change: Nick Butcher (Jetpack Compose) and Jake Wharton (Kotlin/Android) map cleanly to native Android; only their criteria need updating.
- Persistence choice (SQLDelight vs Room) remains an open, separately-reversible decision (future ADR 002).
- `/review`'s `mobile/**/*.kt` routing continues to work unchanged for the new module.
