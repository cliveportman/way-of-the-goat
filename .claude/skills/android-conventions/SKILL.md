---
name: android-conventions
description: Architecture, state management, file naming, error handling, and testing patterns for native Android (Kotlin + Jetpack Compose) code in Way of the Goat.
---

# Android Conventions

Reference for the native Android (Kotlin + Jetpack Compose) patterns used in Way of the Goat. Read before writing or reviewing any app code.

The app is **single-module, Android-only, local and offline** — no accounts, networking, or sync.

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
       Room
```

**Layer rules:**
- Composables: UI only. No business logic. No direct data access.
- ViewModels: Screen state and user action handling. Use `viewModelScope`. No direct DB calls.
- Repositories: DB abstraction over Room DAOs. Always return `Result<T>`. Never throw.
- DataManagers: In-memory singletons with `StateFlow`. Caches state across the session.

---

## Module Structure

Single Gradle module: `app/`.

```
app/src/main/kotlin/co/theportman/way_of_the_goat/
├── screens/           # *Screen.kt composables + *ViewModel.kt
├── data/
│   ├── scoring/       # ScoreCalculator, SuiteDefinitions, FoodCategory (pure Kotlin)
│   ├── cache/         # *DataManager.kt — in-memory StateFlow caches
│   ├── repository/    # *Repository.kt — DB abstraction
│   └── database/      # Room entities, DAOs, @Database
└── ui/theme/          # Color.kt, Spacing.kt, Typography.kt, Theme.kt
```

Local JVM unit tests live in `app/src/test/kotlin/` mirroring the source structure. Instrumented tests, if any, live in `app/src/androidTest/kotlin/`.

---

## File Naming

| Type | Pattern | Example |
|---|---|---|
| Screen composable | `*Screen.kt` | `ScoresScreen.kt` |
| ViewModel | `*ViewModel.kt` | `ScoresViewModel.kt` |
| Repository | `*Repository.kt` | `ServingsRepository.kt` |
| In-memory cache | `*DataManager.kt` | `ServingsDataManager.kt` |
| Room DAO | `*Dao.kt` | `ServingDao.kt` |
| Room entity | `*Entity.kt` | `ServingEntity.kt` |

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
    servingDao.getByDate(date.toString()).map { it.toServing() }
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

The ported scoring core uses `kotlinx.datetime` (`LocalDate`, `Clock`, `TimeZone`). Keep it for the scoring/data layers so the lifted logic stays verbatim and parity holds. `kotlinx-datetime` is a normal Android dependency.

```kotlin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
```

---

## Room

The persistence layer is **Room** (see ADR 002). Schema lives in `data/database/` as `@Entity`, `@Dao`, and a `@Database` class. Room generates DAO implementations via KSP at build time.

```kotlin
@Entity(tableName = "servings")
data class ServingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // ISO LocalDate
    val categoryId: Long,
    val count: Int
)

@Dao
interface ServingDao {
    @Query("SELECT * FROM servings WHERE date = :date")
    suspend fun getByDate(date: String): List<ServingEntity>

    @Insert
    suspend fun insert(serving: ServingEntity): Long
}

@Database(entities = [ServingEntity::class], version = 1)
abstract class WayOfTheGoatDatabase : RoomDatabase() {
    abstract fun servingDao(): ServingDao
}
```

- DAO functions are `suspend` (or return `Flow<T>` for observable queries) so Room runs them off the main thread.
- Keep the `ServingsRepository` interface stable; the repository maps Room entities to/from domain models.
- Bump `@Database(version = …)` and supply a migration for any schema change — never ship a destructive migration to release.

---

## Testing

Tests mirror the source directory structure in `app/src/test/kotlin/`.

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

Use fakes for repositories in unit tests — not a real Room database. The scoring engine is pure Kotlin and tests with no Android dependencies.

Run tests:
```bash
./gradlew :app:testDebugUnitTest      # Local JVM unit tests
```

---

## Build Commands

Run from the repository root (where the Gradle wrapper lives).

```bash
./gradlew detekt                    # Static analysis — must pass with 0 violations
./gradlew :app:assembleDebug        # Build debug APK
./gradlew :app:testDebugUnitTest    # Run local unit tests
./gradlew :app:bundleRelease        # Build release AAB (for Play upload)
./gradlew clean                     # Clean build
```

---

## Release Notes

- **Preserve `applicationId = "co.theportman.way_of_the_goat"`** and the existing signing key — required to update the live Play listing.
- The app is offline: **do not declare the `INTERNET` permission** in `AndroidManifest.xml`.
