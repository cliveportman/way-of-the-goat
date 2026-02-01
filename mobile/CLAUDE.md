# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentation

**Use Notion for project documentation.** Before starting work, search Notion for relevant context:

- Use `mcp__notion__notion-search` to find design docs, requirements, and decisions
- Use `mcp__notion__notion-fetch` to read full page content
- Search for terms like "Way of the Goat", "WOTG", or specific feature names

### Documentation Structure

Way of the Goat project documentation is organized in Notion with a hierarchical structure under the main "Way of the Goat" page, split into Technical & Development and Business & Marketing sections.

#### Technical & Development Databases

**Features Database** - Tracks development of new capabilities and major enhancements
- **Properties:** Feature (title), Priority, Complexity, Type, Status, Screen (multi-select), Target Date, Notes, PR Link, Related Issues (relation)
- **Workflow:** Each feature page contains three sub-pages:
    1. **Research & Design** - Requirements gathering, approach decisions, technical research, current implementation analysis, problem statement, data model changes
    2. **Implementation Plan** - Detailed tasks, phased breakdown (Database → Repository → DataManager → ViewModel → UI), file-by-file changes with line references, testing checklist
    3. **Retrospective** - What happened, what worked/didn't work, lessons learned, follow-up items, future enhancements
- **Status flow:** Planned → In Progress → Merged → Released
- **Use for:** Planning and building new features from scratch, major enhancements to existing functionality

**Issues & Fixes Database** - Tracks bugs, code review findings, and technical debt requiring investigation
- **Properties:** Issue (title), Severity, Source, Status, Screen (multi-select), Discovered Date, Resolved Date, Notes, PR Link, Related Feature (relation)
- **Workflow:** Each issue page contains three sub-pages:
    1. **Investigation** - Root cause analysis, reproduction steps, impact assessment, debugging findings
    2. **Fix Plan** - Proposed solution, implementation notes, tasks, testing strategy, migration considerations
    3. **Retrospective** - Resolution summary, lessons learned, prevention strategies, follow-up items
- **Status flow:** Identified → Investigating → Fix Planned → Fixing → Verified → Closed
- **Source types:** Testing (found during manual/automated testing), Code Review (found during PR review or static analysis), User Report (reported by users), Technical Debt (identified improvement needs), Refactoring (issues uncovered during refactoring)
- **Use for:** Problems discovered during or after feature development, CodeRabbit or manual code review findings, bugs reported by users or found in testing, technical debt that requires investigation

#### When to Use Each Database

**Create a Feature when:**
- Starting work on a new capability
- Planning a major enhancement
- Work is being planned from the beginning

**Create an Issue when:**
- Testing reveals problems in supposedly-complete features
- Code review (CodeRabbit, peer review) identifies problems
- Users report bugs
- Technical debt surfaces that needs investigation
- Refactoring uncovers issues

**Quick decision rule:** If you're building something new → Feature. If you're fixing something that should already work → Issue.

#### Database Integration

**Two-way relationship:** The Features and Issues databases are related:
- Issues can link to Related Feature (which feature spawned this issue)
- Features display Related Issues (all issues spawned from this feature)
- This enables tracking feature quality and understanding what problems emerged from each feature

**PR tracking:** Both databases include a PR Link property (URL type) for linking to GitHub pull requests. Add this as soon as the PR is created, not just in the retrospective.

**Large initiatives:** For complex projects like comprehensive code reviews that will spawn multiple issues:
1. Create a parent project page under Technical & Development (e.g., "Comprehensive Code Review Project")
2. Create sub-pages for each review phase or area (e.g., "Architecture Review", "Testing Coverage Review")
3. Document findings in phase sub-pages
4. Create individual issues in the Issues & Fixes database for each actionable finding
5. Set Source = "Code Review" for all issues from this initiative
6. Track progress using database views filtered by Source and grouped by Status or Severity

**Additional Documentation:** There's a **Documentation Guide** page under Technical & Development that explains when to use each system with detailed scenario examples and decision trees.

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