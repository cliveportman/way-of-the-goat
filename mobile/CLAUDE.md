# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentation

Project documentation lives in `docs/` at the repo root. See `.claude/skills/docs-conventions/SKILL.md` for formats and conventions.

- **Features:** `docs/features/{name}/` — research.md, plan.md, retro.md
- **Issues:** `docs/issues/{name}/` — investigation.md, fix-plan.md, retro.md
- **Decisions:** `docs/decisions/NNN-short-title.md` (Architecture Decision Records)

When implementing a named feature, read `docs/features/{name}/plan.md` if it exists — it describes the intended approach and phased tasks. Do not browse `docs/` speculatively.

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
└── ui/theme/          # Material3 theme, typography, design tokens
```

**SQLDelight schema:** `composeApp/src/commonMain/sqldelight/.../WayOfTheGoatDatabase.sq`

## Tech Stack

- **UI:** Compose Multiplatform + Material Design 3
- **Database:** SQLDelight (generates code to `build/generated/sqldelight/`)
- **Networking:** Ktor Client (OkHttp on Android, Darwin on iOS)
- **State:** ViewModel + StateFlow/SharedFlow
- **Date/Time:** kotlinx.datetime (use `LocalDate`, not `java.time`)
- **Testing:** kotlin-test, kotlinx-coroutines-test, Turbine

## Design Tokens

Design tokens are sourced from Figma (`wotg - core library`) and committed at `/design-tokens/tokens.json` (repo root). The Kotlin representation lives in `ui/theme/`:

| File | Contents |
|---|---|
| `Color.kt` | `GoatPalette` (all primitives) · `GoatColorScheme` data class · `GoatDarkColorScheme` · `GoatLightColorScheme` |
| `Spacing.kt` | `GoatSpacing` · `GoatSizing` (inc. `Icon`, `Touch`) · `GoatRadius` · `GoatStroke` |
| `Typography.kt` | `GoatTypography` — all 15 M3 slots mapped 1:1 from Figma text styles |
| `Theme.kt` | `WayOfTheGoatTheme` · `LocalGoatColors` · `MaterialTheme.goatColors` extension |

### Usage rules

**Always use tokens — never hardcode colours, spacing, or type sizes.**

```kotlin
// ✅ Semantic colour via goatColors extension
MaterialTheme.goatColors.scorePlus2
MaterialTheme.goatColors.surface

// ✅ M3 colour (picked up automatically by Material components)
MaterialTheme.colorScheme.primary

// ✅ Spacing
GoatSpacing.s16
GoatSizing.Touch.default
GoatRadius.md
GoatStroke.emphasis

// ✅ Typography (via MaterialTheme — GoatTypography is wired in)
MaterialTheme.typography.bodyMedium

// ❌ Never do this
Color(0xFF9AE600)
16.dp   // as a magic number — use GoatSpacing.s16
```

**`title/small` is uppercase in Figma.** Apply `.uppercase()` on the string at the call site — `TextStyle` has no `textTransform`.

### Dark / light theme

`WayOfTheGoatTheme` defaults to `darkTheme = true`. In `App.kt` it is driven by `isSystemInDarkTheme()`. Both `GoatDarkColorScheme` and `GoatLightColorScheme` are defined; `MaterialTheme.goatColors` always reflects the active scheme.

### Design token preview screen

A scrollable preview of all tokens (colours, typography, spacing, sizing, radius, stroke) is available in debug builds via **Help → Design tokens**. Use it to verify tokens resolve correctly on device.

### Updating tokens

When the Figma file changes:
1. Re-export `tokens.json` and `textStyles` from Figma and update `/design-tokens/tokens.json`
2. Update the relevant `Color.kt` / `Spacing.kt` / `Typography.kt` constants to match
3. Run `./gradlew :composeApp:assembleDebug` to verify the build

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