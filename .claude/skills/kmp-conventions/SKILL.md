---
name: kmp-conventions
description: Architecture, state management, file naming, error handling, and testing patterns for Kotlin Multiplatform and Compose Multiplatform code in Way of the Goat.
---

# KMP Conventions

Reference for Kotlin Multiplatform and Compose Multiplatform patterns used in Way of the Goat. Read before writing or reviewing any mobile code.

---

## Architecture

**Pattern:** MVVM + Repository

```
@Composable Screen
        ↓ collectAsStateWithLifecycle()
    ViewModel (StateFlow)
        ↓ suspend functions
    Repository / DataManager
        ↓
  SQLDelight / Ktor Client
```

**Layer rules:**
- Composables: UI only. No business logic. No direct data access.
- ViewModels: Screen state and user action handling. Use `viewModelScope`. No direct DB/network calls.
- Repositories: DB and network abstraction. Always return `Result<T>`. Never throw.
- DataManagers: In-memory singletons with `StateFlow`. Caches state across the session.

---

## Source Sets

| Set | Purpose |
|---|---|
| `commonMain/` | 95%+ of the codebase — all shared logic and UI |
| `androidMain/` | Android `actual` implementations |
| `iosMain/` | iOS `actual` implementations |
| `commonTest/` | All shared unit tests |

**Never** put Android or iOS imports in `commonMain`. Use `expect`/`actual`.

---

## Key Directories

```
composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/
├── screens/           # *Screen.kt composables + *ViewModel.kt
├── data/
│   ├── scoring/       # ScoreCalculator, SuiteDefinitions, FoodCategory
│   ├── cache/         # *DataManager.kt — in-memory StateFlow caches
│   ├── repository/    # *Repository.kt — DB/API abstraction
│   ├── database/      # SQLDelight expect declarations
│   └── remote/        # Ktor API clients
└── ui/theme/          # Color.kt, Spacing.kt, Typography.kt, Theme.kt
```

---

## File Naming

| Type | Pattern | Example |
|---|---|---|
| Screen composable | `*Screen.kt` | `ScoresScreen.kt` |
| ViewModel | `*ViewModel.kt` | `ScoresViewModel.kt` |
| Repository | `*Repository.kt` | `ServingsRepository.kt` |
| In-memory cache | `*DataManager.kt` | `ServingsDataManager.kt` |
| Ktor client | `*ApiClient.kt` | `NutritionApiClient.kt` |

---

## State Management

### ViewModel pattern

```kotlin
class ScoresViewModel(
    private val repository: ServingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoresUiState>(ScoresUiState.Loading)
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        repository.getScores().fold(
            onSuccess = { _uiState.value = ScoresUiState.Success(it) },
            onFailure = { _errors.emit(it.message ?: "Failed to load scores") }
        )
    }
}
```

### Sealed UI state

```kotlin
sealed class ScoresUiState {
    data object Loading : ScoresUiState()
    data class Success(val scores: List<DailyScore>) : ScoresUiState()
    data class Error(val message: String) : ScoresUiState()
}
```

### Collecting state in composables

```kotlin
@Composable
fun ScoresScreen(viewModel: ScoresViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

Use `collectAsStateWithLifecycle()` — not `collectAsState()`. It stops collecting when the composable is not active.

### One-shot events

```kotlin
// ViewModel — navigation or error toasts
private val _navigateTo = MutableSharedFlow<Screen>(extraBufferCapacity = 1)
val navigateTo: SharedFlow<Screen> = _navigateTo.asSharedFlow()
```

---

## Type Safety

### Value classes for IDs

```kotlin
@JvmInline value class CategoryId(val value: Long)
@JvmInline value class SuiteId(val value: Long)
```

Never use raw `Long` for domain identifiers.

### Sealed classes for closed hierarchies

```kotlin
// Prefer sealed class over open class for states and results
sealed class ProfileChangeResult {
    data object Success : ProfileChangeResult()
    data class Error(val reason: String) : ProfileChangeResult()
    data object RequiresConfirmation : ProfileChangeResult()
}
```

---

## Error Handling

```kotlin
// Repository — always return Result<T>
suspend fun getServings(date: LocalDate): Result<List<Serving>> = runCatching {
    db.servingQueries.getByDate(date).executeAsList().map { it.toServing() }
}

// ViewModel — fold the result
viewModelScope.launch {
    repository.getServings(today).fold(
        onSuccess = { _uiState.value = UiState.Success(it) },
        onFailure = { _errors.emit(it.message ?: "Unknown error") }
    )
}
```

---

## Design Tokens

Design tokens live in `ui/theme/`. **Never hardcode colours, spacing, or type sizes.**

| File | What it contains |
|---|---|
| `Color.kt` | `GoatPalette` (primitives), `GoatColorScheme`, `GoatDarkColorScheme`, `GoatLightColorScheme` |
| `Spacing.kt` | `GoatSpacing`, `GoatSizing` (inc. `Icon`, `Touch`), `GoatRadius`, `GoatStroke` |
| `Typography.kt` | `GoatTypography` — 15 M3 slots mapped from Figma |
| `Theme.kt` | `WayOfTheGoatTheme`, `LocalGoatColors`, `MaterialTheme.goatColors` extension |

```kotlin
// ✅ Always use tokens
MaterialTheme.goatColors.scorePlus2
MaterialTheme.goatColors.surface
GoatSpacing.s16
GoatSizing.Touch.default
GoatRadius.md
GoatStroke.emphasis
MaterialTheme.typography.bodyMedium

// ❌ Never hardcode
Color(0xFF9AE600)
16.dp
RoundedCornerShape(8.dp)
```

**Note:** `title/small` is uppercase in Figma. Apply `.uppercase()` at the call site — `TextStyle` has no `textTransform`.

---

## Date and Time

Use `kotlinx.datetime` throughout `commonMain`. Never use `java.time`.

```kotlin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
```

---

## SQLDelight

Schema: `composeApp/src/commonMain/sqldelight/.../WayOfTheGoatDatabase.sq`

Generated code lives in `build/generated/sqldelight/` — never modify it directly.

Regenerate after schema changes:
```bash
./gradlew :composeApp:generateCommonMainWayOfTheGoatDatabaseInterface
```

---

## Ktor Client

Ktor clients live in `data/remote/`. Platform-specific engines configured via `expect`/`actual`.

```kotlin
// commonMain — expect declaration
expect fun httpClient(): HttpClient

// androidMain — OkHttp engine
actual fun httpClient(): HttpClient = HttpClient(OkHttp) { /* config */ }

// iosMain — Darwin engine
actual fun httpClient(): HttpClient = HttpClient(Darwin) { /* config */ }
```

---

## Testing

Tests mirror the source directory structure in `composeApp/src/commonTest/kotlin/`.

```kotlin
// Suspend function test
@Test
fun `calculateDailyScore with empty servings returns zero`() = runTest {
    val result = calculator.calculateDailyScore(emptyList())
    assertEquals(0, result)
}

// StateFlow test with Turbine
@Test
fun `uiState emits Loading then Success on init`() = runTest {
    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        assertEquals(UiState.Success(expected), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

Test naming: backtick style — `fun \`given X when Y then Z\`()`.

Run tests:
```bash
./gradlew :composeApp:jvmTest           # Fast — common tests only
./gradlew :composeApp:allTests          # All platforms (slower)
```

---

## Platform-Specific Code

```kotlin
// commonMain — declare the expectation
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain — Android implementation
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(WayOfTheGoatDatabase.Schema, context, "wayofthegoat.db")
}

// iosMain — iOS implementation
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(WayOfTheGoatDatabase.Schema, "wayofthegoat.db")
}
```

---

## Build Commands

```bash
./gradlew :composeApp:assembleDebug     # Build Android debug APK
./gradlew :composeApp:jvmTest           # Run common tests
./gradlew :composeApp:allTests          # Run all tests
./gradlew :composeApp:generateCommonMainWayOfTheGoatDatabaseInterface   # Regenerate SQLDelight
./gradlew clean                         # Clean build
```

iOS: Open `iosApp/iosApp.xcodeproj` in Xcode.
