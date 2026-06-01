# Native Android Rewrite (Food Scoring Only) — Implementation Plan

**Status:** In progress — Phases 1–5 complete; Phase 6 (release prep) next
**Date:** 2026-05-31
**Branch:** `docs/adr-native-android-pivot` (Phases 1–4 landed here)

> Decision context: see [ADR 001](../../decisions/001-kmp-to-native-android-food-scoring.md). This plan is the implementation contract; the ADR is the rationale.

## Progress

- **Phase 1 ✅** `.claude` tooling retargeted to native Android (`android-conventions`/`android-review-criteria` skills, `jake-wharton`/`nick-butcher` agents, CLAUDE.md).
- **Phase 2 ✅** KMP frozen at `references/kmp/`; RN v1.4.3 lifted to `references/react-native/`. No active Gradle root references KMP.
- **Phase 3 ✅** `android/` Gradle root + `:app` module scaffolded; `applicationId`/SDKs matched to the live listing; no `INTERNET` permission; runs on emulator (placeholder UI).
- **Phase 4 ✅** Scoring core ported verbatim (parity tests pass); Room persistence ([ADR 002](../../decisions/002-room-over-sqldelight.md)) behind the stable `ServingsRepository` interface, with fake-DAO unit tests; activity stack dropped. `:app:testDebugUnitTest` and `:app:assembleDebug` both green; Room schema v1 exported to `android/app/schemas/`.
- **Phase 5 ✅** Scoring-only UI ported from `references/kmp/.../screens/`. Verified green: `detekt` (47 files, 0 smells), `:app:assembleDebug`, and `:app:testDebugUnitTest` all pass. Full Goat theme/token port (Color/Spacing/Typography/Theme + Inter fonts in `res/font/`) replacing the Material 3 placeholder; all screens, components, and ViewModels ported near-verbatim against the stable Phase 4 data layer; navigation graph + first-run onboarding wired. Dropped `RunWeekRow`, `RunsOverTimeScreen`, `DesignTokensScreen` per ADR 001. **`HomeScreen` is retained** — it is the branded first-run welcome/splash, not a redundant surface (see ADR 001 amendment).
- **Phase 6 ⬜** Release prep — versioning, release signing keystore (see the TODO in `android/app/build.gradle.kts`), AAB, internal testing track.

## Goal

Replace the Kotlin Multiplatform app with a single-module **native Android** app (Kotlin + Jetpack Compose), scoped to **food scoring only** and fully local/offline. Port the existing scoring engine near-verbatim; drop the entire Intervals.icu activity stack.

## Scope (from ADR 001)

**Keep:** scoring engine + suites; food-serving logging + local DB; Scores screen (launch surface); Score history (Scores Over Time); Help/user guide; first-run onboarding; suite/profile switching.

**Cut:** all of `data/auth`, `data/remote`, activity half of `data/cache` (`ActivityDataManager`, `ActivityDataSource`), `IntervalsRepository`, `RunsOverTimeScreen` + VM, `RunWeekRow`, `DesignTokensScreen`. No accounts, networking, or sync. (`HomeScreen` was originally listed here but is **retained** — it is the branded first-run welcome/splash; see ADR 001 amendment.)

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

- [x] Scores screen + `ScoresViewModel` as the launch surface for returning users.
- [x] Scores Over Time + VM.
- [x] Help screen; first-run welcome (`HomeScreen` + `GoatMoon`) and onboarding (`IntroFlowScreen`).
- [x] Suite/profile switching (`ProfileSwitcherSheet`).
- [x] Port reusable components: `FoodScoringComponents`, `ScoreWeekRow`, `WeekRowShared`, `DataLossConfirmationDialog`, `ContinueButton`. Drop `RunWeekRow`.
- [x] Theme/token port: `Color`, `Spacing`, `Typography`, `Theme` + Inter fonts in `res/font/`.
- [x] Navigation: returning launch → Scores; first launch → Home → IntroFlow → Scores (gated by a persisted onboarding flag in `OnboardingPreferences`).
- [x] Verify on a machine with the Android SDK: `./gradlew detekt :app:assembleDebug :app:testDebugUnitTest` — all green.

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
