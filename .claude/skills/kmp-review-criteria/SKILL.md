# KMP Review Criteria

Review standards for Kotlin Multiplatform and Compose Multiplatform code in the Way of the Goat mobile app. Used by the `nick-butcher` reviewer agent and as a coding target for the `jake-wharton` implementer agent.

---

## Architecture

- [ ] MVVM + Repository layers respected: Composable → ViewModel → Repository → SQLDelight/Ktor
- [ ] No business logic in `@Composable` functions
- [ ] No direct SQLDelight queries or Ktor calls in ViewModels
- [ ] Repositories return `Result<T>` — no exceptions thrown from data layer
- [ ] `viewModelScope` used for coroutines in ViewModels (not `GlobalScope`)
- [ ] DataManagers (`*DataManager.kt`) are singletons with `StateFlow` — not used for one-off network calls

---

## Compose Best Practices

- [ ] `modifier: Modifier = Modifier` present on all composable functions (including screens)
- [ ] State collected with `collectAsStateWithLifecycle()` — not `collectAsState()`
- [ ] ViewModels not passed as parameters to child composables
- [ ] Data and lambda callbacks passed down to children (state hoisting)
- [ ] `Surface` (or `Card`) used for clickable containers — not `Box`/`Column` with `.clickable {}`
- [ ] `@Preview` provided for each new composable — at minimum a default and a dark variant
- [ ] Preview functions are `private` and wrapped in `WayOfTheGoatTheme`
- [ ] No `@Composable` lambda capturing mutable state incorrectly (prefer lambdas that read state at call site)

---

## Design Tokens — No Hardcoding

Flag any hardcoded value that should be a design token:

```kotlin
// ❌ Flag these
Color(0xFF9AE600)
Color.Red
16.dp                          // bare magic number — use GoatSpacing.s16
RoundedCornerShape(8.dp)       // use GoatRadius.md
fontSize = 14.sp               // use MaterialTheme.typography.*
fontWeight = FontWeight.Bold   // should come from the typography token
```

```kotlin
// ✅ Correct usage
MaterialTheme.goatColors.scorePlus2
MaterialTheme.colorScheme.surface
GoatSpacing.s16
GoatSizing.Touch.default
GoatRadius.md
GoatStroke.emphasis
MaterialTheme.typography.bodyMedium
```

---

## State Management

- [ ] `MutableStateFlow` is `private`; `StateFlow` is the public API (`asStateFlow()`)
- [ ] `MutableSharedFlow` is `private`; `SharedFlow` is the public API (`asSharedFlow()`)
- [ ] One-shot events (errors, navigation triggers) use `SharedFlow` — not `StateFlow`
- [ ] Sealed classes used for screen UI states (`Loading`, `Success`, `Error`)
- [ ] Value classes used for domain IDs (`CategoryId`, `SuiteId`) — not raw `Long`
- [ ] No shared mutable state between composables outside of a ViewModel

---

## Coroutines

- [ ] All suspend calls are in the data layer or ViewModel (not composables)
- [ ] No blocking calls on the main thread (`Thread.sleep`, `runBlocking` in production code)
- [ ] `runTest` used in tests — not bare coroutine builders
- [ ] Turbine used for `StateFlow`/`SharedFlow` testing — not manual `collect` loops with delays
- [ ] Cancellation handled: no fire-and-forget work without a scope

---

## Error Handling

- [ ] `runCatching { }` used at data layer boundaries
- [ ] `Result<T>` propagated up — not caught and swallowed silently
- [ ] ViewModel maps `onFailure` to a user-visible error state or `SharedFlow` event
- [ ] No `try/catch(Exception e) { /* ignored */ }` without a comment explaining why

---

## Testing

- [ ] Tests in `commonTest/` mirroring the source structure
- [ ] Test naming: backtick style — `fun \`given X when Y then Z\``
- [ ] `runTest` used for all coroutine tests
- [ ] Turbine used for Flow testing
- [ ] Mocks/fakes used for repositories — not real SQLDelight databases in unit tests
- [ ] Each new public function in the data layer has at least one test

---

## Accessibility

- [ ] Icons that convey meaning have `contentDescription`
- [ ] Decorative icons have `contentDescription = null`
- [ ] `Modifier.semantics { }` used on complex interactive components that need custom descriptions
- [ ] Touch targets meet `GoatSizing.Touch.default` (48dp) for all interactive elements
- [ ] `Modifier.minimumInteractiveComponentSize()` applied to small interactive elements where needed

---

## Kotlin Quality

- [ ] No `!!` (non-null assertion) without a comment justifying it
- [ ] No `var` where `val` would work
- [ ] `data class` used for value types
- [ ] `sealed class`/`sealed interface` for closed hierarchies — not open classes
- [ ] `@JvmInline value class` for domain IDs
- [ ] No raw `Long`/`Int`/`String` used for typed domain concepts
- [ ] Explicit visibility modifiers (`private`, `internal`) where appropriate

---

## KMP-Specific

- [ ] `commonMain` has no Android (`android.*`) or iOS (`platform.`) imports
- [ ] Platform differences use `expect`/`actual` — not runtime platform checks
- [ ] `kotlinx.datetime` used throughout — not `java.time.*`
- [ ] No Java-only types in `commonMain`

---

## File Conventions

- [ ] Screen composables in `screens/` named `*Screen.kt`
- [ ] ViewModels in `screens/` named `*ViewModel.kt`
- [ ] Repositories in `data/repository/` named `*Repository.kt`
- [ ] DataManagers in `data/cache/` named `*DataManager.kt`
- [ ] New files in `commonMain` unless the code is genuinely platform-specific
