# Runs Over Time — Implementation Plan

**Status:** Planning
**Date:** 2026-04-04
**Branch:** kmp

## Goal

Replace the existing Progress screen (third bottom-nav tab) with a new "Runs Over Time" screen that shows daily endurance activity distances in a scrollable weekly grid, mirroring the structure of the existing Scores Over Time screen.

## Phases

### Phase 1: Data models

- [ ] Create `DayActivity` data class with `date: LocalDate`, `dayName: String`, `distance: Double`, `activityCount: Int`
- [ ] Create `WeekActivityData` data class with `dateRangeLabel: String`, `dailyActivities: List<DayActivity?>`, `weeklyTotalKm: Double`

**Files to create/modify:**
- `data/scoring/model/DayActivity.kt` — new file, parallel to `DayScore.kt`
- `data/scoring/model/WeekActivityData.kt` — new file, parallel to `WeekScoreData.kt`

**Notes:**
- `DayActivity.distance` is in km (converted from the `Activity.distance` metres field at the builder layer, not the model layer)
- `DayActivity.activityCount` is needed separately from distance so the tile can render 1–3 dots
- `weeklyTotalKm` is `Double` not `Int`, matching the distance domain — format at the UI layer with one decimal place where needed (e.g. "86.6 km", "77 km"), reusing the `formatDistance` helper already present in `ProgressScreen.kt`

---

### Phase 2: WeeklyActivityBuilder

- [ ] Create `WeeklyActivityBuilder`, a stateless class that mirrors `WeeklyScoreBuilder`
- [ ] Inject `Clock` and `TimeZone` for testability (same pattern as `WeeklyScoreBuilder`)
- [ ] `buildWeeks(activities: List<Activity>): List<WeekActivityData>?` — groups raw `Activity` objects by week (Mon–Sun), builds `WeekActivityData` per week, returns null if list is empty
- [ ] `buildWeekData(weekMonday: LocalDate, activities: List<Activity>): WeekActivityData` — internal method, maps Mon–Sun date offsets to daily aggregates
- [ ] Per-day aggregation: filter activities whose `startDateLocal` parses to the day's date, sum `distance` (metres → km), count activities for dots
- [ ] Reuse `WeeklyScoreBuilder.getMonday`, `WeeklyScoreBuilder.formatDateRange`, and `WeeklyScoreBuilder.dayNameForDayOfWeek` (all are `companion object` functions — call them directly rather than duplicating)
- [ ] A day with no activities maps to `null` (blank tile)

**Files to create:**
- `data/scoring/WeeklyActivityBuilder.kt` — new file, parallel to `WeeklyScoreBuilder.kt`

**Notes:**
- `Activity.startDateLocal` is an ISO 8601 string; parse the date portion with `.substringBefore('T')` and compare to `date.toString()`, matching the existing pattern in `ActivityDataManager`
- `Activity.distance` is nullable (`Double?`); treat `null` as `0.0` with `?: 0.0`
- Activities with `distance == null` and `distance == 0.0` still count toward `activityCount` (an activity occurred even if distance is unknown)

---

### Phase 3: RunsOverTimeViewModel

- [ ] Create `RunsOverTimeViewModel` mirroring `ScoresOverTimeViewModel`
- [ ] Inject `ActivityDataManager` (object singleton), `WeeklyActivityBuilder`, `Clock`, `TimeZone`
- [ ] Expose `uiState: StateFlow<RunsOverTimeUiState>`
- [ ] Expose `errors: SharedFlow<String>` for error snackbar events
- [ ] `init` calls `loadDataAndObserve()`:
  - Sets state to `Loading`
  - Calls `ActivityDataManager.loadInitialData(aroundDate = today, bufferDays = 91)` — 91 days gives ~13 weeks of history, matching the servings window used by `ScoresOverTimeViewModel`
  - On success: collects `ActivityDataManager.activitiesFlow` and rebuilds on each emission
  - On failure: sets `Error` state and emits to `errors`
- [ ] `rebuildFromActivities(activities: List<Activity>)` — delegates to `WeeklyActivityBuilder.buildWeeks`, sets `Success` or `Empty`
- [ ] Define `RunsOverTimeUiState` sealed class: `Loading`, `Success(weeks: List<WeekActivityData>)`, `Empty`, `Error(message: String)`

**Files to create:**
- `screens/RunsOverTimeViewModel.kt` — new file

**Notes:**
- `ActivityDataManager` is an `object` singleton without a `ServingsDataSource`-style interface. For testability the `activitiesFlow` can be injected as a constructor parameter (`StateFlow<List<Activity>>`) with a default of `ActivityDataManager.activitiesFlow`, and `loadInitialData` abstracted similarly. Alternatively, accept `ActivityDataManager` directly and mock it in tests via a simple wrapper interface. Evaluate which approach matches the established pattern in the codebase — if testing `ProgressViewModel` does not currently inject the data manager, a thin `ActivityDataSource` interface (mirroring `ServingsDataSource`) may be the cleanest path.
- `ActivityDataManager` does not expose an `isInitialized` flag equivalent to `ServingsDataManager`. The ViewModel should proceed straight to `loadInitialData` without that guard — if the manager returns an empty list, `WeeklyActivityBuilder.buildWeeks` returns null and the state transitions to `Empty`.

---

### Phase 4: RunWeekRow composable

- [ ] Create `RunWeekRow` composable in `screens/components/RunWeekRow.kt`
- [ ] Signature: `RunWeekRow(weekData: WeekActivityData, modifier: Modifier = Modifier)` — no click callback (display-only per spec)
- [ ] Structure mirrors `ScoreWeekRow`: outer `Column`, header `Row` (date range left, weekly total right), then tiles `Row`
- [ ] Header: date range `bodySmall`/`onSurface`, weekly total formatted as "{n} km" with `bodySmall`/`onSurface`
- [ ] Tiles row: `Arrangement.spacedBy(GoatSpacing.s4)`, height `GoatSizing.Touch.default`
- [ ] Data tile: `Box`, `weight(1f)`, `Modifier.height(GoatSizing.Touch.default)`, background `MaterialTheme.colorScheme.onSurface` (#F8FAFC), sharp corners (`RectangleShape`)
  - Distance text: `titleMedium`, `MaterialTheme.colorScheme.surface` (#020618, dark text on bright tile), centered
  - Activity dots: small `Row` of up to 3 `Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface))`, `Modifier.align(Alignment.BottomEnd).padding(4.dp)`, 2dp gap between dots
- [ ] Blank tile: `Box`, `weight(1f)`, background `MaterialTheme.goatColors.surfaceContainerHigh`, no content — reuse the same `BlankTile`-style private composable pattern from `ScoreWeekRow`
- [ ] Accessibility: tile `contentDescription` = `"{dayName}: {distance} km, {n} activities"` or `"{dayName}: no activity"`; row `contentDescription` = `"Week of {dateRangeLabel}"`
- [ ] Use `Surface` with `shape = RectangleShape` for tiles (consistent with `ScoreWeekRow` — `Surface` handles ripple and semantics cleanly)
- [ ] Distance number formatting: reuse the same `formatDistance` logic from `ProgressScreen.kt` — extract it to a shared location if needed, or duplicate it with a `// Same as ProgressScreen.formatDistance` comment and a TODO to consolidate
- [ ] Previews: dark, light, all-blank

**Files to create:**
- `screens/components/RunWeekRow.kt` — new file

**Notes on dot rendering:** The spec calls for `Box` items inside a `Row` with `Modifier.align(Alignment.BottomEnd)` on the containing `Row`. The correct structure is to use a `Box` for the tile content area, with the dots `Row` as a sibling to the distance `Text` inside the `Box`, positioned with `Modifier.align(Alignment.BottomEnd).padding(4.dp)`. The distance text sits at `Alignment.Center`.

---

### Phase 5: RunsOverTimeScreen composable

- [ ] Create `RunsOverTimeScreen` in `screens/RunsOverTimeScreen.kt`
- [ ] Outer composable wires ViewModel, collects `uiState`, hosts `SnackbarHost` for errors — mirrors `ScoresOverTimeScreen` structure exactly
- [ ] Inner `RunsOverTimeContent` composable takes `uiState: RunsOverTimeUiState` (no callbacks needed)
- [ ] Layout: `Column`, horizontal padding `GoatSpacing.s12`, top `Spacer` of `GoatSpacing.s32`, title `headlineLarge`/`onSurface` with `semantics { heading() }`, `Spacer(GoatSpacing.s16)`, `DayOfWeekHeaders()`, `Spacer(GoatSpacing.s16)`, then state-driven content
- [ ] `DayOfWeekHeaders` is identical to the one in `ScoresOverTimeScreen` — copy the private composable verbatim (both files have it as a `private fun`; it's small enough that deduplication is not worth the indirection)
- [ ] State rendering:
  - `Loading` → centred `CircularProgressIndicator(color = MaterialTheme.goatColors.onSurface)`
  - `Success` → `LazyColumn(verticalArrangement = Arrangement.spacedBy(GoatSpacing.s24))` with `items(key = { it.dateRangeLabel }) { RunWeekRow(it) }` — note spacing is `s24` (not `s12` like Scores Over Time)
  - `Empty` → centred `Text("No runs yet. Start logging activities to see your distance here.", bodyMedium, onSurfaceVariant)`
  - `Error` → centred `Text(message, bodyMedium, onSurfaceVariant)`
- [ ] Background: `MaterialTheme.goatColors.surface` on the content `Column` (matches Scores Over Time)
- [ ] Previews: dark, light, loading, empty

**Files to create:**
- `screens/RunsOverTimeScreen.kt` — new file

---

### Phase 6: Navigation wiring

- [ ] Add `RunsOverTime` route to `Screen.kt` sealed class: `data object RunsOverTime : Screen("runs_over_time")`
- [ ] Replace `Screen.Progress` entry in `bottomNavItems` with `Screen.RunsOverTime`, label `"Runs"`, icon `Icons.Filled.ShowChart` (same icon as Progress currently uses — confirm if a different icon is preferred)
- [ ] Update `showBottomNav` set in `App.kt` to replace `Screen.Progress.route` with `Screen.RunsOverTime.route`
- [ ] Add `composable(Screen.RunsOverTime.route) { RunsOverTimeScreen() }` to the `NavHost` in `App.kt`
- [ ] Remove `Screen.Progress` from `Screen.kt` (sealed class entry and `bottomNavItems` reference)
- [ ] Remove `composable(Screen.Progress.route) { ProgressScreen() }` from `App.kt` NavHost
- [ ] Delete `ProgressScreen.kt` composable file
- [ ] Delete `ProgressViewModel.kt` file
- [ ] Delete any Progress-related test files (e.g. `ProgressViewModelTest.kt`)
- [ ] Extract `formatDistance` from `ProgressScreen.kt` to a shared utility before deleting (needed by `RunWeekRow` — do this in Phase 4)

**Files to modify:**
- `Screen.kt` — add `RunsOverTime` screen object; remove `Progress` screen object; update `bottomNavItems`
- `App.kt` — update `showBottomNav`, replace `Progress` composable route with `RunsOverTime`, update imports

**Files to delete:**
- `ProgressScreen.kt`
- `ProgressViewModel.kt`
- Any Progress-related test files

---

### Phase 7: Tests

- [ ] Create `FakeActivityDataSource` test utility (mirrors `FakeServingsDataSource`) — wraps `activitiesFlow: StateFlow<List<Activity>>` and provides controllable `loadInitialData`
- [ ] Create `RunsOverTimeViewModelTest` with the same test scenarios as `ScoresOverTimeViewModelTest`:
  - `Loading → Success` on successful load with activities
  - `Loading → Empty` on successful load with no activities
  - `Loading → Error` on load failure
  - Error event emitted to `errors` flow on load failure
  - Rebuilds UI state when `activitiesFlow` emits a new list
- [ ] Create `WeeklyActivityBuilderTest`:
  - Empty input returns null
  - Single activity on a Wednesday produces a week with null Mon, Tue, data Wed, null Thu–Sun
  - Multiple activities on the same day sum their distances and count correctly
  - Activity with `distance == null` counts toward `activityCount` but contributes `0.0` to distance
  - Week spanning month boundary formats the date range correctly (delegates to `WeeklyScoreBuilder.formatDateRange`, but worth an integration check)
  - Most-recent week appears first in result list

**Files to create:**
- `commonTest/.../testutils/FakeActivityDataSource.kt`
- `commonTest/.../screens/RunsOverTimeViewModelTest.kt`
- `commonTest/.../data/scoring/WeeklyActivityBuilderTest.kt`

## Testing Strategy

- Run `./gradlew :composeApp:jvmTest` after each phase to catch regressions early
- ViewModel tests use `Turbine` + `StandardTestDispatcher`, matching the `ScoresOverTimeViewModelTest` pattern
- Builder tests are pure unit tests with no coroutines needed
- Manual smoke test: build debug APK, navigate to the third tab, verify tiles render with correct distances and dot counts, verify loading indicator appears on first open, verify the week rows scroll with the day headers staying fixed

## Dependencies

- Design specs: `design-specs/screens/runs-over-time.md`, `design-specs/components/run-week-row.md`
- No new data layer work: `ActivityDataManager` and `IntervalsRepository` are already in place
- `WeeklyScoreBuilder` companion functions (`getMonday`, `formatDateRange`, `dayNameForDayOfWeek`) are reused directly — no changes to that class needed

## Risks

- **`ActivityDataManager` is not easily injectable:** It is an `object` singleton without a matching interface. To keep `RunsOverTimeViewModel` testable, either introduce a thin `ActivityDataSource` interface (analogous to `ServingsDataSource`) or inject the `activitiesFlow` and a load lambda as constructor parameters. The latter is lighter-weight but less conventional in this codebase. Recommendation: introduce `ActivityDataSource` interface — it also makes a future `ProgressViewModel` refactor easier.
- **Distance number formatting:** The spec shows both `"8"` and `"8.3"` patterns. The `formatDistance` helper in `ProgressScreen.kt` already handles this correctly (strips trailing `.0`). Reuse it rather than implementing fresh formatting logic. If it is extracted to a shared utility in a later cleanup, the `RunsOverTimeScreen.kt` usage will be straightforward to update.
- **Row spacing difference:** Runs Over Time uses `GoatSpacing.s24` between rows; Scores Over Time uses `GoatSpacing.s12`. This is intentional per spec. The plan above already captures this, but it is an easy thing to copy-paste incorrectly from the existing screen — the diff in the `LazyColumn` `verticalArrangement` parameter is the only place it matters.
- **`formatDistance` extraction:** `ProgressScreen.kt` contains a `formatDistance` helper needed by the new `RunWeekRow`. Extract it to a shared utility in Phase 4 before deleting `ProgressScreen.kt` in Phase 6.
