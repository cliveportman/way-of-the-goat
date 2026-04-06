---
name: kmp-review-criteria
description: Checklist for reviewing KMP and Compose Multiplatform code — architecture, design tokens, state management, testing, and accessibility.
---

# KMP Review Criteria

Checklist for reviewing Kotlin Multiplatform and Compose Multiplatform code in the Way of the Goat mobile app. Used by `nick-butcher` (reviewer) and `jake-wharton` (implementer).

For code examples, patterns, and detailed conventions, see the `kmp-conventions` skill (`kmp-conventions/SKILL.md`).

---

## Static Analysis

From the `mobile/` directory, run `./gradlew detekt` before reviewing. It must pass clean.

- [ ] `./gradlew detekt` passes with 0 violations
- [ ] No new `@Suppress("detekt...")` annotations without a comment justifying the suppression

If detekt fails, address the violations before proceeding with the rest of the review.

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

Flag any hardcoded colour, spacing, radius, or type size. All values must come from the design token system in `ui/theme/`. See `kmp-conventions` for the full token reference table and usage examples.

- [ ] No `Color(0x...)` or `Color.Red` — use `MaterialTheme.goatColors.*` or `MaterialTheme.colorScheme.*`
- [ ] No bare `.dp` literals for spacing — use `GoatSpacing.*`
- [ ] No `RoundedCornerShape(n.dp)` — use `GoatRadius.*`
- [ ] No `fontSize = n.sp` or `fontWeight = ...` — use `MaterialTheme.typography.*`

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
- [ ] `@Preview` in `commonMain` uses `org.jetbrains.compose.ui.tooling.preview.Preview` — **not** `androidx.compose.ui.tooling.preview.Preview` (Android-only, breaks KMP compilation)
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
