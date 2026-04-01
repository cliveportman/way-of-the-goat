Implement a Compose Multiplatform spec from: $ARGUMENTS

# Task: Implement from Design Spec

## Purpose

Transform a curated design spec (`.md`) into production-ready Compose Multiplatform code. The spec's **Type** field determines the scope:

- **Component** → `@Composable` function + `@Preview` + `kotlin-test` tests
- **Screen** → Screen composable + ViewModel + `@Preview` + tests
- **Flow** → All referenced screen implementations + NavGraph routes + shared ViewModel (if needed)

The spec is the single source of truth — follow it precisely.

## Prerequisites

- Curated design spec exists at the path in `$ARGUMENTS`
- `design-specs/tokens.json` exists for token-to-value reference
- **Screen specs:** All referenced component specs should be implemented (flag any that aren't)
- **Flow specs:** All referenced screen specs should be curated (flag any that aren't)

---

## Workflow Steps

### Step 0: Read the Design Spec

1. Read the spec file at the path provided
2. Identify the **Type** field: `Component`, `Screen`, or `Flow`
3. Read `design-specs/tokens.json` for token-to-Kotlin mappings

| Spec Type | Workflow |
|---|---|
| Component | Steps 1–6 |
| Screen | Steps 1–6 (with dependency check at Step 1) |
| Flow | Step F1–F5, then Steps 1–6 per screen |

---

### Step 1: Discover Project Conventions

Before generating anything, inspect the target project:

1. **Find existing composables** — list `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/screens/` and read 1–2 existing files to learn:
   - File structure (one composable per file vs multiple)
   - How `modifier` is handled
   - How state is collected (`collectAsStateWithLifecycle`)
   - Existing composable helper functions in scope

2. **Find existing components** — read 1–2 files from the app to understand:
   - How `Surface` vs `Card` is used for containers
   - How `GoatSpacing`, `GoatRadius`, `MaterialTheme.goatColors` are referenced
   - Existing reusable composables to use instead of creating new ones

3. **Check for existing tokens in use** — scan `ui/theme/` for available `GoatSpacing.*`, `GoatRadius.*`, `GoatStroke.*` values to match against spec

4. **If spec type is Screen** — check component dependencies:
   - Read the Section Layout table from the spec
   - For each referenced component spec, check if a matching `.kt` file exists
   - Flag unimplemented components:

   ```
   ## Dependency Check

   ✅ score-card — implemented at screens/components/ScoreCard.kt
   ⚠️ food-category-item — spec exists but not yet implemented
   ❌ weekly-summary — no spec found

   Shall I implement the missing component first, or proceed with a placeholder?
   ```

   Wait for decision. If implementing the dependency first, run Steps 2–6 for that component then resume.

---

### Step 2: Plan the Implementation

Present a brief plan before writing code.

#### Component Plan

```
## Implementation Plan

**Spec type:** Component
**Composable:** `ScoreCard`
**File:** `mobile/composeApp/src/commonMain/kotlin/.../screens/components/ScoreCard.kt`
**Tests:** `mobile/composeApp/src/commonTest/kotlin/.../screens/components/ScoreCardTest.kt`

**Parameters:**
- `score: DailyScore` — the score data to display
- `onClick: () -> Unit` — called when card is tapped
- `modifier: Modifier = Modifier`

**Previews:**
- Default (dark)
- Light theme
- Negative score variant

Shall I proceed?
```

#### Screen Plan

```
## Implementation Plan

**Spec type:** Screen
**Screen composable:** `ScoresScreen`
**ViewModel:** `ScoresViewModel`
**Files:**
- `mobile/.../screens/scores/ScoresScreen.kt`
- `mobile/.../screens/scores/ScoresViewModel.kt`
**Tests:**
- `mobile/.../screens/scores/ScoresViewModelTest.kt`

**ViewModel state:** `ScoresUiState` sealed class (Loading, Success, Error)
**ViewModel events:** `SharedFlow<String>` for error messages

Shall I proceed?
```

Wait for user confirmation before writing any code.

---

### Step 3: Implement

#### Component Implementation

1. Create the composable file in `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/`
2. Follow all conventions from `.claude/skills/kmp-conventions/SKILL.md`:
   - Parameters: data → callbacks → `modifier: Modifier = Modifier`
   - All colours via `MaterialTheme.colorScheme.*` or `MaterialTheme.goatColors.*`
   - All spacing via `GoatSpacing.*`
   - All radius via `GoatRadius.*`
   - `Surface` for clickable containers
   - `contentDescription` for icons

3. Write `@Preview` functions immediately below the composable:
   - Default dark theme
   - Light theme variant (`darkTheme = false`)
   - Any meaningful state variants from the spec

```kotlin
@Preview
@Composable
private fun ScoreCardPreview() {
    WayOfTheGoatTheme {
        ScoreCard(
            score = DailyScore(/* ... */),
            onClick = {}
        )
    }
}

@Preview(name = "Light")
@Composable
private fun ScoreCardPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        ScoreCard(
            score = DailyScore(/* ... */),
            onClick = {}
        )
    }
}
```

4. Write tests in `commonTest/` mirroring the source path:

```kotlin
class ScoreCardTest {
    @Test
    fun `given positive score renders with correct content description`() {
        // Compose UI tests via composeTestRule (if available)
        // or logic unit tests if the composable contains extractable logic
    }
}
```

#### Screen Implementation

1. Create a sealed `UiState` class (unless one already exists for this screen)
2. Create the `ViewModel` — private `MutableStateFlow`, public `StateFlow`, `viewModelScope.launch` for loading:

```kotlin
class ScoresViewModel(
    private val repository: ServingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoresUiState>(ScoresUiState.Loading)
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        repository.getScores().fold(
            onSuccess = { _uiState.value = ScoresUiState.Success(it) },
            onFailure = { _uiState.value = ScoresUiState.Error(it.message ?: "Error") }
        )
    }
}
```

3. Create the Screen composable — collect state, render each section per the Section Layout table:

```kotlin
@Composable
fun ScoresScreen(
    viewModel: ScoresViewModel,
    onNavigateToDetail: (CategoryId) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ScoresUiState.Loading -> { /* skeleton / spinner */ }
        is ScoresUiState.Success -> { /* compose sections from spec */ }
        is ScoresUiState.Error -> { /* error state */ }
    }
}
```

4. Write ViewModel tests — use `runTest` and Turbine:

```kotlin
class ScoresViewModelTest {
    @Test
    fun `uiState emits Loading then Success when load succeeds`() = runTest {
        val fakeRepository = FakeServingsRepository(success = true)
        val viewModel = ScoresViewModel(fakeRepository)

        viewModel.uiState.test {
            assertEquals(ScoresUiState.Loading, awaitItem())
            assertIs<ScoresUiState.Success>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

### Flow Implementation Steps (F1–F5)

#### F1: Audit Screen Dependencies

Read the flow spec. For each screen spec it references:
- Check if a matching `.kt` screen file exists
- List what needs to be created

```
## Flow Implementation Audit

The flow references {n} screens:

✅ WelcomeScreen — implemented
⚠️ FoodEntryScreen — spec exists, not implemented
⚠️ ConfirmationScreen — spec exists, not implemented

I'll implement the missing screens in dependency order, then wire up navigation.
```

#### F2: Implement Screens in Order

Run the Component and Screen implementation steps (Steps 1–6) for each unimplemented screen.

#### F3: Create Navigation Routes

Add route constants and NavHost entries per the Implementation Notes in the flow spec:

```kotlin
// Navigation destinations
object FoodLogRoutes {
    const val ENTRY = "food_log/entry"
    const val CONFIRM = "food_log/confirm/{date}"

    fun confirm(date: LocalDate) = "food_log/confirm/$date"
}
```

#### F4: Shared ViewModel (if needed)

If the flow spec defines shared state that crosses screens, create a shared ViewModel scoped to the NavGraph's BackStackEntry:

```kotlin
// In NavHost composable
val sharedViewModel: FoodLogViewModel = hiltViewModel(
    viewModelStoreOwner = remember(navController) {
        navController.getBackStackEntry(FoodLogRoutes.ENTRY)
    }
)
```

#### F5: Note Manual Wiring

List any wiring that requires the user to connect the NavHost manually:

```
## Navigation Wiring Required

The following changes need to be made to your NavHost manually:

1. Add the food log NavGraph or composable destinations to your main NavHost
2. Add a navigation trigger from the Dashboard screen to `FoodLogRoutes.ENTRY`

Implementation files are complete — only the NavHost wiring remains.
```

---

### Step 4: Verify

After implementing, run:

```bash
# Build (catches compile errors)
./gradlew :composeApp:assembleDebug

# Tests
./gradlew :composeApp:jvmTest
```

If the build fails: fix errors before presenting results.

---

### Step 5: Present Results

```
## Implementation Complete

**Files created:**
- `mobile/.../screens/scores/ScoresScreen.kt`
- `mobile/.../screens/scores/ScoresViewModel.kt`
- `mobile/.../screens/scores/ScoresViewModelTest.kt`

**Build:** ✅ Passed
**Tests:** ✅ {n} tests passing

{if flow:}
**Manual wiring needed:**
{list any NavHost changes required}
```

---

## Quality Checklist

- [ ] No hardcoded colours, spacing, or type sizes — all via tokens
- [ ] `modifier: Modifier = Modifier` on all composables
- [ ] `collectAsStateWithLifecycle()` used in composables
- [ ] ViewModels not passed to child composables
- [ ] `@Preview` for each composable (dark + light minimum)
- [ ] ViewModel uses sealed `UiState` class
- [ ] Errors surface via `SharedFlow` — not swallowed
- [ ] Tests use `runTest` + Turbine for Flow assertions
- [ ] Build passes (`./gradlew :composeApp:assembleDebug`)
- [ ] All tests pass (`./gradlew :composeApp:jvmTest`)

## Handling Edge Cases

### Component already exists
Read the existing implementation and check if it matches the spec. If differences are minor (missing state, wrong token), update in place. If substantially different, ask the user before overwriting.

### Spec references a token not in GoatColorScheme or GoatSpacing
Check `ui/theme/Color.kt` and `ui/theme/Spacing.kt` for the closest match. Note any approximation in a comment.

### Repository or DataManager doesn't exist yet
Create a stub with the correct return type (`Result<T>`) and a `TODO()` body. Note in the summary that the data layer implementation is needed.
