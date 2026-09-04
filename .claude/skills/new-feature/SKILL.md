---
name: new-feature
description: Scaffold a new feature screen in this project — UiState, ViewModel, Screen, previews, route, NavHost entry and a ViewModel test — following the conventions in CLAUDE.md. Use when asked to add a screen, add a feature, or create a new tab in this repo.
---

# New feature

Ask for two things if they weren't given: the **feature name** in PascalCase
(`MealList`, `ContactForm`) and its **kind**:

- **read** — displays data loaded from a repository. Template: the Meals
  feature (`ui/feature/meallist`, `ui/feature/mealdetail`).
- **form** — collects input and writes it. Template: the Contact form
  (`ui/feature/contactform`).

Read `CLAUDE.md` first; it is the source of truth and outranks anything here
if the two disagree. Then create the files below, modelling each one on the
corresponding file of the template feature rather than inventing structure.

## Files to create, under `ui/feature/<lowercasename>/`

1. **`<Name>UiState.kt`**
   - *read*: `sealed interface` with `Loading`, `Error(error: UiError)`, and
     `Content(...)`. Put a `transientError: UiError?` on `Content` if the
     screen can refresh; `isRefreshing: Boolean` if it pulls to refresh.
   - *form*: a flat `data class` of field values, per-field
     `@param:StringRes Int?` errors, `submitError`, `isSubmitting`,
     `isSubmitted`.
2. **`<Name>ViewModel.kt`** — `@HiltViewModel`, one `StateFlow<UiState>`, plus
   one method per user action. Screen arguments come from
   `savedStateHandle.toRoute<Destinations.X>()`. Wrap repository calls in
   `runSuspendCatching`, never `runCatching`.
3. **`<Name>Screen.kt`** — two overloads of the same name: a stateful wrapper
   taking `viewModel: X = hiltViewModel()` that collects state and delegates,
   and a stateless one taking `uiState` plus a lambda per action.
4. **`<Name>ScreenPreviews.kt`** — one `@PreviewLightDark` per UiState case,
   wrapped in `ScaffoldTheme`, calling the stateless overload.
5. **`<Name>FormValidation.kt`** (*form* only) — top-level pure functions
   returning `@StringRes Int?`, unit-tested in plain JUnit.

## Wiring

6. Add the route to `ui/navigation/Destinations.kt`. An optional argument is a
   sentinel with a default, never a nullable primitive.
7. Add a `composable<Destinations.X>` entry to `ScaffoldNavHost.kt`, passing
   navigation lambdas in from there.
8. For a new tab: add a `TopLevelDestination` entry (route + `@StringRes`
   label + icon) and pad the screen with `BottomNavigationBarHeight`.
9. Every user-visible string goes in `res/values/strings.xml` as
   `<feature>_<kind>_<descriptor>`. Reuse an existing resource rather than
   duplicating its text.
10. New repository? Interface in `data/repository/`, `@Binds` it in
    `di/RepositoryModule`, take `@param:IoDispatcher` and wrap suspend bodies
    in `withContext`.

## Tests

11. `app/src/test/.../<Name>ViewModelTest.kt` with a hand-written fake
    repository (an in-memory `MutableStateFlow`) and `MainDispatcherRule` —
    no mocking libraries. If the ViewModel reads `toRoute()`, the test needs
    `@RunWith(RobolectricTestRunner::class)`; it still lives in `test/`.
12. Cover each state the screen can be in, not just the happy path: empty
    content, a failed load, and a failed write.

## Finish

Run the `check` skill. Nothing is done until it passes.
