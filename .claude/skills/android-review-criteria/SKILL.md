---
name: android-review-criteria
description: Checklist for reviewing native Android (Kotlin + Jetpack Compose) code — architecture, design tokens, state management, Room, testing, and accessibility.
---

# Android Review Criteria

Checklist for reviewing native Android (Kotlin + Jetpack Compose) code in the Way of the Goat app. Used by `nick-butcher` (reviewer) and `jake-wharton` (implementer).

For code examples, patterns, and detailed conventions, see the `android-conventions` skill (`android-conventions/SKILL.md`).

The app is single-module, Android-only, local and offline — no networking, auth, or sync.

---

## Static Analysis

Run `./gradlew detekt` before reviewing. It must pass clean.

- [ ] `./gradlew detekt` passes with 0 violations
- [ ] No new `@Suppress("detekt...")` annotations without a comment justifying the suppression

If detekt fails, address the violations before proceeding with the rest of the review.

---

## Architecture

- [ ] MVVM + Repository layers respected: Composable → ViewModel → Repository → Room
- [ ] No business logic in `@Composable` functions
- [ ] No direct Room DAO queries in ViewModels
- [ ] Repositories return `Result<T>` — no exceptions thrown from data layer
- [ ] `viewModelScope` used for coroutines in ViewModels (not `GlobalScope`)
- [ ] DataManagers (`*DataManager.kt`) are singletons with `StateFlow` — not used for one-off calls

---

## Compose Best Practices

- [ ] `modifier: Modifier = Modifier` present on all composable functions (including screens)
- [ ] State collected with `collectAsStateWithLifecycle()` — not `collectAsState()`
- [ ] ViewModels not passed as parameters to child composables
- [ ] Data and lambda callbacks passed down to children (state hoisting)
- [ ] `Surface` (or `Card`) used for clickable containers — not `Box`/`Column` with `.clickable {}`
- [ ] `@Preview` provided for each new composable — at minimum a default and a dark variant
- [ ] `@Preview` import is `androidx.compose.ui.tooling.preview.Preview`
- [ ] Preview functions are `private` and wrapped in `WayOfTheGoatTheme`
- [ ] No `@Composable` lambda capturing mutable state incorrectly (prefer lambdas that read state at call site)

---

## Design Tokens — No Hardcoding

Flag any hardcoded colour, spacing, radius, or type size. All values must come from the design token system in `ui/theme/`. See `android-conventions` for the full token reference table and usage examples.

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

## Room

- [ ] Entities, DAOs, and `@Database` live in `data/database/`
- [ ] DAO functions are `suspend` or return `Flow<T>` — no synchronous main-thread queries
- [ ] Schema changes bump `@Database(version = …)` and provide a migration — no destructive migration in release builds
- [ ] Repository maps Room entities to/from domain models; the `ServingsRepository` interface stays stable
- [ ] No Room types (`@Entity`, DAO) leaking above the repository layer into ViewModels or composables

---

## Testing

- [ ] Local unit tests in `app/src/test/` mirroring the source structure
- [ ] Test naming: backtick style — `fun \`given X when Y then Z\``
- [ ] `runTest` used for all coroutine tests
- [ ] Turbine used for Flow testing
- [ ] Fakes used for repositories — not a real Room database in unit tests
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

## Offline Constraint

- [ ] No networking code (Ktor, OkHttp, Retrofit, `java.net`) — the app is fully offline
- [ ] No `INTERNET` permission in `AndroidManifest.xml`
- [ ] No auth, accounts, or sync logic introduced

---

## File Conventions

- [ ] Screen composables in `screens/` named `*Screen.kt`
- [ ] ViewModels in `screens/` named `*ViewModel.kt`
- [ ] Repositories in `data/repository/` named `*Repository.kt`
- [ ] DataManagers in `data/cache/` named `*DataManager.kt`
- [ ] Room DAOs/entities in `data/database/` named `*Dao.kt` / `*Entity.kt`
