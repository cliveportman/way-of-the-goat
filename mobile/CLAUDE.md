# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentation

**Use Notion for project documentation.** Before starting work, search Notion for relevant context:

- Use `mcp__notion__notion-search` to find design docs, requirements, and decisions
- Use `mcp__notion__notion-fetch` to read full page content
- Search for terms like "Way of the Goat", "WOTG", or specific feature names

### Feature Documentation Structure

When starting work on a new feature, sub-feature, or bug fix, create a parent page in the "Way of the Goat - Features" database with three sub-pages:

**1. Research & Design**
- Current implementation analysis (how it works now)
- Problem statement (what's wrong or missing)
- Research questions to investigate
- Design decisions with rationale
- Data model changes if applicable

**2. Implementation Plan**
- Summary of changes
- Phased breakdown (Database → Repository → DataManager → ViewModel → UI)
- File-by-file changes with specific line references
- Testing checklist

**3. Retrospective** (after completion)
- Summary of what was implemented
- What went well
- What could be improved (issues encountered, workarounds)
- Future enhancements identified

See "Past Day Profile Handling" feature in Notion as an example of this structure.

## Build Commands

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Run all tests
./gradlew :composeApp:allTests

# Run common tests only (faster)
./gradlew :composeApp:jvmTest

# Run single test class
./gradlew :composeApp:jvmTest --tests "co.theportman.way_of_the_goat.data.scoring.ScoreCalculatorTest"

# Run single test method
./gradlew :composeApp:jvmTest --tests "*.ScoreCalculatorTest.calculateDailyScore_emptyServings_returnsZero"

# Generate SQLDelight code after schema changes
./gradlew :composeApp:generateCommonMainWayOfTheGoatDatabaseInterface

# Clean build
./gradlew clean
```

**iOS:** Open `iosApp/iosApp.xcodeproj` in Xcode.

## Architecture

**Pattern:** MVVM with Repository pattern

```
Composables → ViewModel (StateFlow) → DataManager/Repository → SQLDelight / Ktor
```

**Source Sets:**
- `commonMain/` - Shared code (95%+ of codebase)
- `androidMain/` - Android `actual` implementations
- `iosMain/` - iOS `actual` implementations
- `commonTest/` - Shared unit tests

## Key Directories

```
composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/
├── screens/           # UI + ViewModels
├── data/
│   ├── scoring/       # Core business logic (ScoreCalculator, SuiteDefinitions)
│   ├── cache/         # In-memory state (ServingsDataManager)
│   ├── repository/    # DB access layer
│   ├── database/      # SQLDelight expect declarations
│   └── remote/        # Ktor API clients
└── ui/theme/          # Material3 theme, typography
```

**SQLDelight schema:** `composeApp/src/commonMain/sqldelight/.../WayOfTheGoatDatabase.sq`

## Tech Stack

- **UI:** Compose Multiplatform + Material Design 3
- **Database:** SQLDelight (generates code to `build/generated/sqldelight/`)
- **Networking:** Ktor Client (OkHttp on Android, Darwin on iOS)
- **State:** ViewModel + StateFlow/SharedFlow
- **Date/Time:** kotlinx.datetime (use `LocalDate`, not `java.time`)
- **Testing:** kotlin-test, kotlinx-coroutines-test, Turbine

## Conventions

**Files:**
- `*Screen.kt` - Composable screens
- `*ViewModel.kt` - Screen state management
- `*DataManager.kt` - Singleton caches with StateFlow
- `*Repository.kt` - Database/API abstraction

**State:**
- Expose `StateFlow<T>` from ViewModels
- Use `MutableStateFlow` internally
- One-shot events (errors) via `SharedFlow`

**Error Handling:**
- Return `Result<T>` from suspend functions in data layer
- ViewModels handle success/failure cases

**Type Safety:**
- Value classes for IDs: `CategoryId`, `SuiteId`
- Sealed classes for UI state: `ScoresUiState.Loading`, `.Success`, `.Error`

## Testing

```kotlin
// Suspend functions
@Test
fun myTest() = runTest {
    val result = repository.getData()
    assertEquals(expected, result)
}

// StateFlow testing with Turbine
@Test
fun flowTest() = runTest {
    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        assertEquals(UiState.Success, awaitItem())
    }
}
```

Test files mirror source structure in `commonTest/kotlin/`.

## Platform-Specific Code

Use `expect`/`actual` for platform differences:

```kotlin
// commonMain
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(...)
}

// iosMain
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(...)
}
```

## Food Scoring System

Core business logic in `data/scoring/`:
- `ScoreCalculator` - Calculates daily/weekly scores from servings
- `SuiteDefinitions` - Predefined profiles (BALANCED, RACING_WEIGHT)
- `FoodCategory` - Category with scoring rules (points per serving, targets)
- `DailyServings` - User's recorded servings for a date

Each day is tied to a scoring suite. Switching profiles on a day with data requires confirmation (data loss).
