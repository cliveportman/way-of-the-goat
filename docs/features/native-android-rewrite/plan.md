# Native Android Rewrite (Food Scoring Only) — Implementation Plan

**Status:** Planning
**Date:** 2026-05-31
**Branch:** TBD (suggest `feature/native-android-rewrite`)

> Decision context: see [ADR 001](../../decisions/001-kmp-to-native-android-food-scoring.md). This plan is the implementation contract; the ADR is the rationale.

## Goal

Replace the Kotlin Multiplatform app with a single-module **native Android** app (Kotlin + Jetpack Compose), scoped to **food scoring only** and fully local/offline. Port the existing scoring engine near-verbatim; drop the entire Intervals.icu activity stack.

## Scope (from ADR 001)

**Keep:** scoring engine + suites; food-serving logging + local DB; Scores screen (launch surface); Score history (Scores Over Time); Help/user guide; first-run onboarding; suite/profile switching.

**Cut:** all of `data/auth`, `data/remote`, activity half of `data/cache` (`ActivityDataManager`, `ActivityDataSource`), `IntervalsRepository`, `RunsOverTimeScreen` + VM, `RunWeekRow`, `HomeScreen`, `DesignTokensScreen`. No accounts, networking, or sync.

## Phases

### Phase 1: Retarget `.claude` tooling for native Android

Do this first — implementation/review agents read these skills, so they must describe native Android before any code is written against them.

- [ ] Rename/repoint `kmp-conventions` → `android-conventions`: single-module structure, Jetpack Compose, ViewModel + StateFlow, file naming, error handling, testing. Strip `expect/actual` and multiplatform content.
- [ ] Rename/repoint `kmp-review-criteria` → `android-review-criteria`: remove multiplatform-specific checks.
- [ ] Update `jake-wharton` (implementation) and `nick-butcher` (review) agents to reference the new skills and drop KMP framing.
- [ ] Update `CLAUDE.md`: agent/skill tables, the (stale) `references/` section, and project overview.

**Files to create/modify:**
- `.claude/skills/android-conventions/SKILL.md` (from `kmp-conventions`)
- `.claude/skills/android-review-criteria/SKILL.md` (from `kmp-review-criteria`)
- `.claude/agents/jake-wharton.md`, `.claude/agents/nick-butcher.md`
- `CLAUDE.md`

### Phase 2: Freeze KMP as reference

- [ ] Move the KMP project out of the active build into a reference location (e.g. `references/kmp/` or `mobile-kmp/`) so the new module builds clean.
- [ ] Ensure root Gradle no longer includes the KMP modules.

**Files to create/modify:**
- Move `mobile/` → reference location
- Root `settings.gradle.kts` / build config

### Phase 3: Scaffold the native Android module

- [ ] New single-module Android app, Kotlin + Jetpack Compose.
- [ ] **Preserve `applicationId = "co.theportman.way_of_the_goat"`** and the existing signing key — required to update the existing Play listing.
- [ ] Match the live listing's min/target SDK.
- [ ] Wire Compose, `lifecycle-viewmodel-compose`, `navigation-compose`, and the chosen persistence lib.
- [ ] Remove the `INTERNET` permission — the app is offline.

**Files to create/modify:**
- New module `app/` (or reuse `mobile/`): `build.gradle.kts`, `AndroidManifest.xml`, `MainActivity.kt`, theme/`design-tokens` wiring

### Phase 4: Port the scoring core

- [ ] Lift `data/scoring/**` verbatim (pure Kotlin: `ScoreCalculator`, `SuiteDefinitions`, `WeeklyActivityBuilder`, `model/*`).
- [ ] Port food persistence: `ServingsRepository`, `ServingsDataManager`/`ServingsDataSource`, and the DB layer. Persistence tech is **Room** ([ADR 002](../../decisions/002-room-over-sqldelight.md), Accepted); keep the `ServingsRepository` interface stable and swap the implementation.
- [ ] Drop the activity-side cache/repository.

**Files to create/modify:**
- `data/scoring/**`, `data/repository/ServingsRepository.kt`, `data/cache/Servings*.kt`, `data/database/*`

### Phase 5: Rebuild the scoring-only UI

- [ ] Scores screen + `ScoresViewModel` as the launch surface (no Home).
- [ ] Scores Over Time + VM.
- [ ] Help screen; first-run onboarding (`IntroFlowScreen`).
- [ ] Suite/profile switching (`ProfileSwitcherSheet`).
- [ ] Port reusable components: `FoodScoringComponents`, `ScoreWeekRow`, `WeekRowShared`, `DataLossConfirmationDialog`. Drop `RunWeekRow`.
- [ ] Navigation: start destination = Scores; onboarding gates first launch only.

**Files to create/modify:**
- `screens/ScoresScreen.kt` (+VM), `screens/ScoresOverTimeScreen.kt` (+VM), `screens/HelpScreen.kt`, `screens/IntroFlowScreen.kt`, `screens/components/*`, navigation graph, theme

### Phase 6: Release prep

- [ ] Bump `versionCode`/`versionName`; configure release signing.
- [ ] Build AAB; ship to an internal testing track before production.
- [ ] (Optional) Add a mobile release mode to `/pr-create`, mirroring the web one.

## Testing Strategy

- Port the existing `ScoreCalculator` unit tests; run `./gradlew :app:testDebugUnitTest`.
- **Scoring parity:** assert ported scores match known reference outputs (from RN v1.4.3 / KMP) for each suite — this is the correctness bar.
- Build: `./gradlew :app:assembleDebug`.
- Manual: log servings across suites, verify scores; first-run onboarding; history view; suite switching.
- Offline: confirm the app functions with no network and no `INTERNET` permission.

## Dependencies

- ADR 001 (accepted). [ADR 002](../../decisions/002-room-over-sqldelight.md) (accepted) — **Room** is the persistence layer.
- `design-tokens/` and `design-specs/` for UI fidelity.
- Existing Play Store listing details: **applicationId and signing keystore** must match.

## Risks

- **Play continuity (critical):** wrong `applicationId` or signing key blocks updating the live listing. Confirm both before Phase 3.
- **What's actually live on Play?** If the production app is RN v1.4.3 (not the KMP build), existing users may have local data in a different format — decide whether to migrate or accept a clean start. Open question to resolve before release.
- **Scoring parity:** ported logic must reproduce reference scores exactly; mitigated by parity unit tests in Phase 4.
- **Tooling drift:** if Phase 1 is skipped, agents will review against KMP criteria and give misleading feedback.
