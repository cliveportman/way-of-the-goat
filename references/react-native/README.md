/usage# Reference Files - Original React Native App

This directory contains the complete source code from the original **Way of the Goat** React Native application. These files serve as reference material for migrating features to the new Kotlin Multiplatform (KMP) implementation.

## What's In This Directory

This is a snapshot of the production React Native app (v1.4.3) that was built with Expo and published to app stores. It includes:
- Complete screen implementations (Progress, Scores, Onboarding, User Guide)
- Food scoring business logic and calculations
- Core utilities, types, and constants
- Custom UI components with Tailwind styling
- SQLite database schema and migrations
- Assets (Inter font family, app icons)

## Original App Technology Stack

### Framework & Platform
- **React Native** with **Expo SDK**
- **Expo Router** for file-based navigation
- **TypeScript** for type safety
- Platform: iOS & Android

### Key Libraries
- `expo-sqlite` - Local database for storing daily servings
- `expo-router` - File-based navigation system
- `nativewind` - Tailwind CSS for React Native styling
- `expo-font` - Custom font loading (Inter family)

### Styling Approach
- Tailwind utility classes via the `tw` prop
- Custom components wrapping core RN primitives (`TwButton`, `TwText`, `TwContainer`)
- Inter font family: light, regular, medium, semi-bold, bold

## Directory Structure

```
references/
├── app/                           # Expo Router screens
│   ├── (app)/                    # Main app group
│   │   ├── progress/             # Weekly progress view
│   │   ├── scores/               # Daily food scoring
│   │   │   └── [day].tsx        # Dynamic route for specific day
│   │   └── user-guide/           # Help documentation
│   ├── onboarding/               # First-time user flow (3 pages)
│   └── _layout.tsx               # Root navigation layout
├── core/                          # Shared utilities & business logic
│   ├── components/               # Reusable UI primitives
│   │   ├── TwButton.tsx
│   │   ├── TwContainer.tsx
│   │   ├── TwLine.tsx
│   │   └── TwText.tsx
│   ├── svgs/                     # SVG components
│   │   └── GoatMoonSvg.tsx
│   ├── constants.ts              # ⭐ Scoring logic & food categories
│   ├── types.ts                  # ⭐ TypeScript type definitions
│   ├── helpers.ts                # Utility functions
│   ├── hooks.ts                  # Custom React hooks
│   ├── enums.ts                  # Food category enums
│   ├── database.ts               # SQLite queries & CRUD operations
│   └── database-migrations.ts    # Schema versioning
├── features/                      # Feature-specific components
│   ├── progress/                 # Progress screen components
│   │   ├── Bar.tsx              # Single day bar in weekly view
│   │   ├── Chart.tsx            # Weekly chart visualization
│   │   ├── DaySummary.tsx       # Day details modal
│   │   ├── List.tsx             # Historical list view
│   │   └── Week.tsx             # 7-day week component
│   └── scores/                   # Scores screen components
│       ├── Day.tsx              # Daily summary header
│       ├── Score.tsx            # ⭐ Single food category row
│       ├── ScoreLabel.tsx       # Food category label
│       └── ScoreServing.tsx     # Individual serving indicator
├── assets/                        # Static resources
│   ├── fonts/                    # Inter font family (5 weights)
│   └── images/                   # App icons & splash screens
├── app.config.js                 # Expo configuration
└── eas.json                      # Expo Application Services config
```

## Key Business Logic Files

### 🔑 `core/constants.ts`
Contains the **entire food scoring algorithm** from Racing Weight methodology:
- `maxScores` object with scoring arrays for 13 food categories
- Each category has 6 serving scores (e.g., `veg: [2, 2, 2, 1, 0, 0]`)
- Categories: vegetables, fruit, nuts, whole grains, dairy, lean proteins, beverages, refined grains, sweets, fatty proteins, fried foods, alcohol, other
- `defaultServings` object for initializing new day entries

**Migration Priority:** 🔴 HIGH - This logic must be replicated exactly in KMP

### 🔑 `core/types.ts`
TypeScript type definitions:
- `Servings` - Daily food intake record (13 properties + date + id)
- `SingleServingScore` - Union type: `-2 | -1 | 0 | 1 | 2`
- `PossibleSingleServingScores` - Tuple of 6 serving scores
- `DayTotalsForDisplay` - Calculated daily summary (healthy, unhealthy, total, portions)
- `DayTotalsForMaths` - Numeric version for calculations
- `DateString` - Strictly typed date format: `YYYY-MM-DD`

**Migration Priority:** 🔴 HIGH - Core data models

### 🔑 `features/scores/components/Score.tsx`
The main food category input component:
- Displays food category name and 6 serving indicators
- Handles press (add serving) and long-press (remove serving) interactions
- Calculates visual state based on current servings count
- Supports half servings (0.5) for future enhancement
- Maps `maxScores` array to visual serving indicators

**Migration Priority:** 🟡 MEDIUM - Reference for KMP UI implementation

### 🔑 `core/database.ts`
SQLite operations for local storage:
- Table schema for servings data
- CRUD operations (create, read, update, delete)
- Date-based queries for retrieving daily/weekly data
- Migrations support

**Migration Priority:** 🟡 MEDIUM - Will use different persistence in KMP

## Migration Status

### ✅ Completed (New KMP App)
- [x] Basic screen structure (Progress, Scores/Today, Help, Home)
- [x] Bottom navigation with Material3
- [x] Jetpack Compose UI framework
- [x] Navigation between screens
- [x] Pull-to-refresh functionality
- [x] Horizontal pagers for date navigation
- [x] intervals.icu API integration for activity tracking
- [x] Data caching layer
- [x] Authentication providers

### ⏳ Pending Migration (Reference from Old App)
- [ ] **Food scoring system** (constants.ts logic)
- [ ] **Food category data models** (Servings, scores, totals)
- [ ] **Daily servings tracking** (add/remove servings UI)
- [ ] **Score calculation algorithms** (healthy/unhealthy/total)
- [ ] **Local data persistence** (SQLite → KMP equivalent)
- [ ] **Onboarding flow** (3-page introduction)
- [ ] **User guide/help content**
- [ ] **Historical data display** (list view in Progress)
- [ ] **Weekly chart visualization**
- [ ] **Day summary modal** (from Progress bar tap)
- [ ] **Custom font integration** (Inter family)
- [ ] **App icons & branding**

### 🔄 Key Architectural Differences

| Aspect | Old React Native App | New KMP App |
|--------|---------------------|-------------|
| **Primary Function** | Food intake tracking with scoring | Activity/wellness tracking via intervals.icu |
| **Data Source** | Local SQLite database | intervals.icu API + local cache |
| **Navigation** | Expo Router (file-based) | Jetpack Compose Navigation (code-based) |
| **UI Framework** | React Native + Tailwind | Jetpack Compose + Material3 |
| **State Management** | React hooks + local state | ViewModel + StateFlow |
| **Language** | TypeScript | Kotlin |
| **Screens** | Progress, Scores, Onboarding, User Guide | Progress, Today (Scores), Help, Home |
| **Bottom Nav** | 3 tabs (Progress, Today, Guide) | 3 tabs (Progress, Today, Help) |

## How to Use These References

### For Feature Migration
1. **Start with business logic**: Review `core/constants.ts` for scoring rules
2. **Understand data models**: Check `core/types.ts` for structure
3. **Study UI patterns**: Look at feature components for interaction patterns
4. **Replicate calculations**: Ensure scoring math matches exactly

### For UI Design Reference
1. **Component structure**: See how features are composed from smaller components
2. **Interaction patterns**: Press/long-press for incrementing/decrementing
3. **Layout decisions**: Weekly grid, daily list, serving indicators
4. **Color coding**: Score-based visual feedback (green/red/neutral)

### For Data Flow
1. **Database operations**: Understand CRUD patterns in `database.ts`
2. **State management**: Review how screens fetch and update data
3. **Date handling**: See how date calculations work for weekly views

## Important Notes

### Scoring Algorithm Accuracy
The scoring system in `constants.ts` is based on the **Racing Weight** methodology by Matt Fitzgerald. These values are calibrated for endurance athletes and should be replicated **exactly** in the KMP implementation.

### Half Servings Support
The old app has code for 0.5 servings (see `Score.tsx` line 37), but this feature was never fully implemented.

**KMP Implementation Approach**:
- The codebase will **continue to support half servings** (0.5 increments) in the data models and scoring calculations
- The UI will **NOT expose half-serving functionality** in Phase 1-8
- This will be implemented in a later phase once core functionality is stable
- Database schema uses `Double` for servings count to maintain this flexibility

### Date Format Consistency
The old app uses `YYYY-MM-DD` string format throughout. The new KMP app uses `kotlinx.datetime.LocalDate`. Ensure proper conversion when migrating.

### Inter Font Family
The old app uses 5 weights of Inter: light, regular, medium, semi-bold, bold. Font files are in `assets/fonts/` for reference. The new app will need to integrate these or choose an alternative.

## Version Information

- **App Version**: 1.4.3
- **React Native**: Expo SDK
- **Bundle Identifier**: `co.cliveportman.wotg`
- **Last Updated**: April 2025 (reference snapshot)

---

## KMP Implementation Plan - Multi-Suite Food Scoring System

This section outlines the plan for implementing the food scoring system in the new KMP app with **multiple scoring suites** support (Racing Weight, Ketogenic, Coeliac, Endurance 45+).

### Design Overview

**Core Principle**: Scoring suites are **configuration** (defined in code), not user data (stored in database).

**Key Features**:
- Multiple scoring suites with different food categories and scoring rules
- Users can select a suite per day
- Suite selection locks after first food entry for that day
- Historical accuracy preserved (suite_id + version stored per day)
- Clean separation from intervals.icu activity tracking

### Architecture Decision

**Suites as Code Configuration**:
```kotlin
object ScoringRulesFactory {
    fun getAllSuites(): List<ScoringSuite>
    fun getSuiteById(id: String): ScoringSuite?
}

// Suites defined as:
- racingWeightSuite() // Original 13 categories
- ketogenicSuite()    // Low-carb focused
- coeliacSuite()      // Gluten-free safe
- endurance45Suite()  // Age-adjusted for masters athletes
```

**Database Stores Minimal Data**:
```sql
table DayRecord {
    id: Long PRIMARY KEY
    date: String UNIQUE         -- YYYY-MM-DD
    suite_id: String           -- "racing_weight", "ketogenic"
    suite_version: Int         -- 1 (for future versioning)
}

table DayServing {
    id: Long PRIMARY KEY
    day_record_id: Long        -- Foreign key to DayRecord
    category_id: String        -- "vegetables", "healthy_fats"
    servings: Double           -- 3.5 (supports half servings)
}
```

### Data Models

```kotlin
// Domain models
data class ScoringRule(
    val servingScores: List<Int>  // [2, 2, 2, 1, 0, 0]
)

data class FoodCategory(
    val id: String,              // "vegetables"
    val displayName: String,     // "Vegetables"
    val displayOrder: Int,
    val scoringRule: ScoringRule
)

data class ScoringSuite(
    val id: String,              // "racing_weight"
    val name: String,            // "Racing Weight (Official)"
    val description: String,
    val categories: List<FoodCategory>,
    val version: Int = 1
)

data class DayTotals(
    val healthy: Int,
    val unhealthy: Int,
    val total: Int,
    val portions: Int
)
```

### Suite Design Constraints

**All scoring suites must follow these rules**:

1. **Category Count Consistency**: Each suite must have **exactly the same total number of categories** (13 categories)
   - This ensures UI consistency across suite switches
   - Maintains comparable scoring complexity
   - Simplifies UI rendering logic

2. **Point Total Consistency**: All **positive scores** must total **exactly 32 points** per suite
   - This is the **theoretical maximum** if a user only ate healthy foods
   - In practice, users will score **much lower** because they also eat unhealthy foods (which give negative points)
   - Actual daily total = (sum of positive scores) - (sum of negative scores)
   - This normalizes the "healthy eating" ceiling across all suites
   - Allows fair comparison between different dietary approaches
   - Negative scores can vary per suite based on dietary philosophy

3. **Category Structure**: Each category must have exactly 6 serving positions with integer scores
   - Scoring array format: `[score1, score2, score3, score4, score5, score6]`
   - Individual scores can be: -2, -1, 0, 1, or 2 points
   - Total for 6 servings = sum of all 6 positions

### Suite Examples

**Racing Weight (Original)**:
- 13 categories: vegetables, fruit, nuts, whole grains, dairy, lean proteins, beverages, refined grains, sweets, fatty proteins, fried foods, alcohol, other
- Direct port from `references/core/constants.ts`
- **Positive point total**: Verify this totals exactly 32 points

**Ketogenic**:
- Focus: healthy fats, proteins, low-carb vegetables
- Penalizes: all grains, sugars, starchy vegetables, high-sugar fruits
- **Categories**: Must have exactly 13 categories (same as Racing Weight)
- **Positive point total**: Must total exactly 32 points

**Coeliac Safe**:
- Separates gluten-free grains from gluten grains
- Cross-contamination risk category
- Safe proteins and processed GF foods tracking
- **Categories**: Must have exactly 13 categories
- **Positive point total**: Must total exactly 32 points

**Endurance 45+**:
- Age-adjusted scoring (more vegetables, protein emphasized)
- Calcium-rich foods for bone health
- Anti-inflammatory foods category
- **Categories**: Must have exactly 13 categories
- **Positive point total**: Must total exactly 32 points

### Business Logic Rules

1. **Suite Selection**:
   - User can change suite when viewing a day with no servings entered
   - Once any serving is added, suite is locked for that day
   - Each day can have a different suite

2. **Validation**:
   ```kotlin
   fun canChangeSuite(date: LocalDate): Boolean {
       val servings = getServingsForDay(date)
       return servings.isEmpty()
   }
   ```

3. **Score Calculation**:
   ```kotlin
   fun calculateScore(servings: Double, rule: ScoringRule): Int {
       // Sum scores for each full serving
       // Handle 0.5 servings (half the score of next serving)
       // Return total for that category
   }
   ```

### Implementation Phases

#### Phase 1: Database Setup (SQLDelight) ✅
- [x] Add SQLDelight dependency to `mobile/composeApp/build.gradle.kts`
- [x] Create `src/commonMain/sqldelight/` directory structure
- [x] Create `WayOfTheGoatDatabase.sq` schema file with tables
- [x] Generate Kotlin code from SQL schema
- [x] Create database drivers for Android/iOS

**Files Created**:
- `commonMain/sqldelight/co/theportman/way_of_the_goat/data/database/WayOfTheGoatDatabase.sq`
- `commonMain/kotlin/.../data/database/DatabaseDriverFactory.kt` (expect)
- `androidMain/kotlin/.../data/database/DatabaseDriverFactory.kt` (actual)
- `iosMain/kotlin/.../data/database/DatabaseDriverFactory.kt` (actual)

#### Phase 2: Core Data Models ✅
- [x] Create `data/scoring/model/` package structure
- [x] Define `ScoringRule`, `FoodCategory`, `ScoringSuite` data classes
- [x] Define `DailyServings` with score calculation
- [x] Create type-safe value classes (`SuiteId`, `CategoryId`)

**Files Created**:
- `data/scoring/model/FoodCategory.kt` (includes ScoringRule)
- `data/scoring/model/ScoringSuite.kt`
- `data/scoring/model/DailyServings.kt`

#### Phase 3: Scoring Suite Configuration ✅
- [x] Create `data/scoring/SuiteDefinitions.kt`
- [x] Implement `RACING_WEIGHT` suite (original from book)
- [x] Implement `BALANCED` suite (default recommendation)
- [x] Implement `BODY_COMPOSITION` suite (weight loss focus)
- [x] Implement `HIGH_LOAD` suite (high training volume)
- [x] Add `allSuites` list and `getSuiteById()` helper

**Files Created**:
- `data/scoring/SuiteDefinitions.kt`

**Note**: Suite definitions evolved from original plan - see `SCORINGSUITES.md` for details

#### Phase 4: Repository Layer ✅
- [x] Create `data/repository/ServingsRepository.kt`
- [x] Implement repository pattern with Result wrapper
- [x] Methods implemented:
  - `getServingsForDate()`, `getServingsForRange()`
  - `saveServings()`, `updateCategoryServings()`
  - `setActiveSuite()`, `calculateScoreForDate()`
  - `deleteServingsForDate()`
- [x] Create `data/scoring/ScoreCalculator.kt` for score calculations

**Files Created**:
- `data/repository/ServingsRepository.kt`
- `data/scoring/ScoreCalculator.kt`

#### Phase 5: Cache Layer ✅
- [x] Create `data/cache/ServingsDataManager.kt` singleton
- [x] Follow `ActivityDataManager` pattern with `StateFlow`
- [x] Implement lazy loading with smart date range tracking
- [x] Expose `servingsFlow: StateFlow<Map<LocalDate, DailyServings>>`
- [x] Methods implemented:
  - `loadInitialData()`, `ensureDateLoaded()`
  - `incrementServings()`, `decrementServings()`
  - `calculateScoreForDate()`

**Files Created**:
- `data/cache/ServingsDataManager.kt`

#### Phase 6: ViewModel Updates ✅
- [x] Update `ScoresViewModel` for food scoring
- [x] Create `ScoresUiState` sealed class (Loading, Success, Error)
- [x] Expose `activeSuite` and `servingsFlow` from data manager
- [x] Implement `incrementServings()`, `decrementServings()`
- [x] Implement date preloading on scroll

**Files Modified**:
- `screens/ScoresViewModel.kt`

#### Phase 7: UI Implementation ✅
- [x] Update `ScoresScreen.kt` with HorizontalPager for date navigation
- [x] Display active suite name with info icon
- [x] Create `FoodCategoryRow` composable with serving cells
- [x] Create `ServingCell` with color-coded backgrounds (+2/+1/0/-1/-2)
- [x] Implement tap to increment, long-press to decrement
- [x] Create `ScoreSummary` with total/healthy/unhealthy breakdown
- [x] Style with Material3 components

**Files Modified/Created**:
- `screens/ScoresScreen.kt`
- `screens/components/FoodScoringComponents.kt`

#### Phase 8: Integration & Testing ✅
- [x] Database initialization in Android `MainActivity`
- [x] Date navigation with lazy loading working
- [x] Pull-to-refresh on scores view
- [x] Default suite: Balanced (configurable)
- [ ] Suite switching UI (pending - currently uses default suite)
- [ ] Half-serving support (deferred to future phase)

### Integration with Existing Architecture

**Follows Established Patterns**:

| Layer | Existing Pattern | Food Scoring Implementation |
|-------|-----------------|----------------------------|
| **Models** | `@Serializable` data classes | Domain models (no serialization needed for suites) |
| **Persistence** | In-memory (intervals.icu API) | SQLDelight local database |
| **Repository** | `IntervalsRepository` with `Result<T>` | `FoodScoringRepository` following same pattern |
| **Cache** | `ActivityDataManager` singleton | `FoodDataManager` singleton with StateFlow |
| **ViewModel** | `StateFlow` + sealed classes | Extend `ScoresViewModel` with food state |
| **UI** | Compose + Material3 | New nutrition view in `ScoresScreen` |

**Coexistence**:
- Food scoring and activity tracking are **completely separate**
- Different data sources (local SQLite vs intervals.icu API)
- Different purposes (nutrition vs training)
- Unified in UI via view mode toggle on Scores/Today screen

### Key Benefits of Option 3

1. ✅ **Clean database schema** - Simple, performant queries
2. ✅ **Type-safe suites** - Defined in Kotlin, compile-time checked
3. ✅ **Easy to extend** - Add new suites by writing code
4. ✅ **Historical accuracy** - Suite ID + version preserved per day
5. ✅ **Flexible categories** - Each suite has unique categories
6. ✅ **Good KMP support** - SQLDelight works seamlessly across platforms
7. ✅ **Follows existing patterns** - Matches IntervalsRepository/ActivityDataManager architecture

### Next Steps

1. **Start with Phase 1**: Set up SQLDelight and create schema
2. **Reference constantly**: Port scoring logic exactly from `references/core/constants.ts`
3. **Test incrementally**: Validate each phase before moving to next
4. **Maintain separation**: Keep food scoring independent from intervals.icu integration

---

## Questions?

If you're working on migrating a feature and need clarification on how it worked in the old app, refer to:
1. The specific component implementation
2. Related helpers in `core/helpers.ts`
3. Type definitions in `core/types.ts`
4. Database schema in `core/database.ts`

The goal is to preserve the user experience and scoring accuracy while modernizing the tech stack.
