# Scaffold improvement tasks

Each task is self-contained: read `CLAUDE.md` first, do the task, then run
the full check suite before considering it done. Tasks are ordered so that
earlier ones don't get invalidated by later ones. Work one task per commit
(or per PR) and keep commit messages terse per the `CLAUDE.md` convention.

Each heading carries a suggested model tag. `[Fable]` marks tasks that
define a convention every derived project inherits, where the `CLAUDE.md`
prose matters as much as the code. `[Opus]` tasks need real Kotlin and
coroutines judgment but the design is already decided. `[Sonnet]` tasks
are fully specified and mechanical. Opus is a fine default for everything
if you'd rather not switch models; Sonnet is not, because of the Fable ones.

Definition of done for every task:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew ktlintFormat ktlintCheck detekt testDebugUnitTest assembleDebug
```

If a task touches `MealRepositoryImpl`, `MealDao`, or `AppDatabase`, also run
`./gradlew connectedDebugAndroidTest` against the `Pixel_9` AVD.

If a task changes or adds a convention, update the matching section of
`CLAUDE.md` in the same commit. `CLAUDE.md` is the source of truth for
derived projects; stale docs are worse than no docs.

---

## Phase 1: Fix the reference implementation

These are bugs in code that derived projects will copy verbatim.

### 1.1 Make the Meals empty state reachable  `[Opus]`

**Problem:** `MealListViewModel` maps an empty list from `observeMeals()` to
`Loading`, so a category with zero results spins forever. The `EmptyState`
branch in `MealListScreen.MealList` is unreachable.

**Do:**
- Track whether a refresh has completed (e.g. a private
  `MutableStateFlow<RefreshStatus>` with `Idle / InFlight / Failed(Throwable)`),
  and `combine` it with `observeMeals()` to derive `uiState`.
- `Loading` only while the cache is empty **and** a refresh is in flight or
  hasn't run yet. Empty cache after a successful refresh is `Content(emptyList())`.
- Add a unit test in `MealListViewModelTest`: fake repository returns an
  empty list after refresh, assert `Content` with empty `meals`.

**Files:** `ui/feature/meallist/MealListViewModel.kt`,
`app/src/test/.../MealListViewModelTest.kt`.

### 1.2 Stop `refreshByCategory` wiping the cache before the fetch  `[Opus]`

**Problem:** `MealRepositoryImpl.refreshByCategory` calls `mealDao.deleteAll()`
before the network call. A failed fetch leaves the user with nothing, which
inverts the offline-first contract the repository exists to demonstrate.

**Do:**
- Fetch first. Only after a successful response, replace the cache in a single
  `@Transaction` DAO method (`replaceAll(meals)` = delete + insert).
- Add an `MealRepositoryEndToEndTest` case: enqueue a 500 response, call
  `refreshByCategory`, assert the previously cached meals are still returned
  by `observeMeals()`.

**Files:** `data/local/MealDao.kt`, `data/repository/MealRepositoryImpl.kt`,
`app/src/androidTest/.../MealRepositoryEndToEndTest.kt`.

### 1.3 Cancel the previous refresh when a category is re-selected  `[Opus]`

**Problem:** `MealListViewModel.selectCategory` launches a new coroutine per
call with no cancellation, so rapid taps interleave two delete/insert pairs.

**Do:**
- Hold a `private var refreshJob: Job?`; cancel it before launching a new one.
- Unit test: select category A then immediately B with a fake repository
  whose refresh suspends; assert only B's meals end up in `Content` and
  `selectedCategory == B`.

**Files:** `ui/feature/meallist/MealListViewModel.kt`, its test.

### 1.4 Replace `runCatching` in ViewModels with cancellation-safe handling  `[Opus]`

**Problem:** `runCatching` catches `CancellationException`, which breaks
structured concurrency. Both `MealListViewModel` and `MealDetailViewModel`
use it.

**Do:**
- Catch `IOException` and `retrofit2.HttpException` explicitly (or add a
  small `suspend inline fun <T> runSuspendCatching` helper in a new
  `util/` package that rethrows `CancellationException`). Pick one and
  document it in `CLAUDE.md`.
- Add a "Don'ts" bullet to `CLAUDE.md`: never `runCatching` around a
  suspend call.

**Files:** both ViewModels, `CLAUDE.md`.

### 1.5 Introduce a `UiError` type instead of raw exception messages  `[Fable]`

**Problem:** `Error(message: String?)` surfaces text like
"Unable to resolve host" or "HTTP 500" directly to the user. This also
violates the spirit of the strings rule.

**Do:**
- Add `ui/components/UiError.kt`: a `sealed interface UiError` with
  `Network`, `Server`, `Unknown` cases, each mapping to a `@StringRes`
  via a `UiError.messageRes()` extension (or a `@StringRes val message`).
- Add `fun Throwable.toUiError(): UiError` next to it (`IOException` ->
  Network, `HttpException` -> Server, else Unknown).
- Change every `<Name>UiState.Error(message: String?)` to
  `Error(error: UiError)`. Composables render with
  `stringResource(state.error.messageRes)`.
- Add strings `error_network`, `error_server`, `error_unknown`; delete the
  three `*_error_fallback` strings.
- Update the `CLAUDE.md` Strings section: remove the paragraph about the
  nullable `String?` message flowing out of a ViewModel and describe
  `UiError` instead.
- Update all affected unit and androidTest tests.

**Files:** all `*UiState.kt`, all ViewModels, all Screens,
`res/values/strings.xml`, `CLAUDE.md`, tests.

### 1.6 Surface refresh failures while content is showing  `[Fable]`

**Problem:** When the cache already has data, both Meals ViewModels silently
drop a refresh failure. There's no transient-feedback pattern in the scaffold.

**Do:**
- Add a `transientError: UiError?` field to `MealListUiState.Content` and
  `MealDetailUiState.Content`, plus a `dismissTransientError()` ViewModel
  method.
- Show it with a `SnackbarHost` in the screen's `Scaffold`, via
  `LaunchedEffect(state.transientError)`; call `dismissTransientError()`
  after `showSnackbar` returns.
- Document this in `CLAUDE.md` under Architecture as *the* pattern for
  one-off UI feedback (state field, not a `Channel`/`SharedFlow` of events;
  explain why: survives rotation, testable via `uiState.value`).
- Unit test: fake repository has cached meals, refresh throws, assert
  `Content.transientError` is `UiError.Unknown` (or Network if the fake
  throws `IOException`).

**Files:** Meals UiStates, ViewModels, Screens, `CLAUDE.md`, tests.

### 1.7 Add a submit-failure path to the form pattern  `[Opus]`

**Problem:** `ContactFormViewModel.submit` assumes `saveContact` succeeds.

**Do:**
- Add `@StringRes val submitError: Int? = null` to `ContactFormUiState`.
- Wrap the repository call; on failure set `isSubmitting = false` and
  `submitError = R.string.contact_form_error_save_failed`.
- Render it as a `Text` in `MaterialTheme.colorScheme.error` above the
  Save button, and clear it on the next `submit()`.
- Unit test with a fake repository whose `saveContact` throws.
- Update the Forms paragraph in `CLAUDE.md`.

**Files:** `ui/feature/contactform/*`, `strings.xml`, `CLAUDE.md`, test.

### 1.8 Export the Room schema and add migration test infrastructure  `[Opus]`

**Problem:** `AppDatabase` has `exportSchema = false`, but `CLAUDE.md`
mandates explicit `Migration`s. Without exported schemas there is nothing
for `MigrationTestHelper` to test against.

**Do:**
- Apply the `androidx.room` Gradle plugin (add to `libs.versions.toml`,
  same version as `room`) and configure `room { schemaDirectory("$projectDir/schemas") }`.
- Set `exportSchema = true`. Commit the generated `app/schemas/.../1.json`.
- Add `androidTestImplementation(libs.androidx.room.testing)`.
- Add `app/src/androidTest/.../data/local/MigrationTest.kt` with a
  `MigrationTestHelper` rule and a single test that opens version 1 and
  asserts it validates. This is the template a future `MIGRATION_1_2` test
  will extend.
- Update the "Schema changes" paragraph in `CLAUDE.md` to mention the
  schema directory and the migration test.

**Files:** `gradle/libs.versions.toml`, `app/build.gradle.kts`,
`AppDatabase.kt`, new `app/schemas/`, new androidTest, `CLAUDE.md`.

### 1.9 Fix the stale keep-rules comment  `[Sonnet]`

`app/src/main/keepRules/rules.keep` references `PostDto.serializer()`, which
no longer exists. Change it to `MealSummaryDto.serializer()`.

---

## Phase 2: Fill scaffold-pattern gaps

### 2.1 Split each screen into a stateful wrapper and a stateless body, add previews  `[Fable]`

**Problem:** Every `<Name>Screen` takes a ViewModel and everything beneath
it is private, so nothing can be `@Preview`ed and UI tests must construct a
ViewModel.

**Do:**
- For each of the four screens, keep the public
  `XScreen(onBack, modifier, viewModel = hiltViewModel())` as a thin wrapper
  that collects state and delegates to a new public stateless
  `XScreen(uiState: XUiState, onBack, onRetry, ..., modifier)` overload.
- Add a `XScreenPreviews.kt` file next to each screen with one
  `@PreviewLightDark` preview per UiState case, wrapped in `ScaffoldTheme`.
  Use `androidx.compose.ui.tooling.preview.PreviewParameterProvider` if it
  keeps the file shorter.
- Rewrite `MealListScreenTest` to call the stateless overload directly with
  a `Content` state; drop its fake repository.
- Document the split in `CLAUDE.md` Architecture: the stateless overload is
  the unit of preview and UI test; the stateful one is the unit of
  navigation.

**Files:** all four `*Screen.kt`, four new `*ScreenPreviews.kt`,
`MealListScreenTest.kt`, `CLAUDE.md`.

### 2.2 Run Android Lint in CI  `[Sonnet]`

**Do:**
- Add `lint { abortOnError = true; warningsAsErrors = true; checkDependencies = false }`
  to `android {}` in `app/build.gradle.kts`.
- Run `./gradlew lintDebug`, fix or baseline what it reports. Prefer fixing.
  If a baseline is unavoidable, commit `app/lint-baseline.xml` and set
  `baseline = file("lint-baseline.xml")`.
- Add `lintDebug` to the CI Gradle invocation in `.github/workflows/ci.yml`
  and to the Static analysis section of `README.md` and `CLAUDE.md`.

### 2.3 Move the base URL into `BuildConfig` and set OkHttp timeouts  `[Sonnet]`

**Do:**
- In `app/build.gradle.kts` `defaultConfig`, add
  `buildConfigField("String", "MEAL_API_BASE_URL", "\"https://www.themealdb.com/api/json/v1/1/\"")`.
- `NetworkModule` reads `BuildConfig.MEAL_API_BASE_URL`; delete the
  private `BASE_URL` const.
- Add `connectTimeout`/`readTimeout` of 30s to the `OkHttpClient` builder
  as named constants.
- Add a short `CLAUDE.md` note under Data layer: per-environment config
  lives in `buildConfigField`, overridden per build type or flavor, never
  as a Kotlin constant.

### 2.4 Add dispatcher injection  `[Opus]`

**Do:**
- Add `di/CoroutinesModule.kt` providing `@IoDispatcher CoroutineDispatcher`
  (a `@Qualifier` annotation in the same file).
- `MealRepositoryImpl` and `ContactRepositoryImpl` take
  `@IoDispatcher private val ioDispatcher: CoroutineDispatcher` and wrap
  their suspend bodies in `withContext(ioDispatcher)`.
- `ScaffoldApp` should stop building its own `Dispatchers.IO` scope and
  instead inject the dispatcher (or better: move seeding to a
  `RoomDatabase.Callback.onCreate` in `DatabaseModule`; pick one, document
  the choice).
- `MealRepositoryEndToEndTest` passes `UnconfinedTestDispatcher()`.
- Document in `CLAUDE.md` Dependency injection.

### 2.5 Complete Contacts CRUD (edit and delete)  `[Opus]`

**Do:**
- `ContactDao`: add `@Update`, `@Delete`, and `observeContact(id): Flow<ContactEntity?>`.
- `ContactRepository`: add `observeContact(id)`, `updateContact`, `deleteContact`.
- `Destinations.ContactForm` becomes `data class ContactForm(val contactId: Long? = null)`.
  `ContactFormViewModel` reads it via `SavedStateHandle.toRoute()`; when
  non-null it preloads the fields and `submit()` calls `updateContact`.
  Because of the `toRoute()` limitation described in `CLAUDE.md`, move
  `ContactFormViewModelTest` to `androidTest` (mirror `MealDetailViewModelTest`)
  or keep the JVM test by injecting the id through a small
  `ContactFormArgs` wrapper. Pick one and say why in `CLAUDE.md`.
- `ContactListScreen`: tapping a row opens the form in edit mode; swipe to
  dismiss (`SwipeToDismissBox`) deletes, with an undo snackbar using the
  transient-feedback pattern from 1.6.
- Add strings for the edit title, delete confirmation, and undo.
- Tests for update, delete, and the edit-mode preload.

### 2.6 Use format strings for composed user-facing text  `[Sonnet]`

`ContactListScreen.ContactRow` builds "First Last" and "City Postcode" with
string templates. Replace with `stringResource(R.string.contact_list_full_name, first, last)`
and `contact_list_city_postcode`. Add a `CLAUDE.md` Strings bullet: any
user-visible text assembled from parts uses a format resource, never
Kotlin string templates.

### 2.7 Adopt Robolectric for JVM Compose and `toRoute()` tests  `[Fable]`

**Decision task.** Evaluate moving `MealDetailViewModelTest` and
`MealListScreenTest` from `androidTest` to `test` using Robolectric
(`testOptions { unitTests.isIncludeAndroidResources = true }`,
`testImplementation("org.robolectric:robolectric:<latest>")`).

- If it works cleanly and the JVM test run stays under ~30s, do it, update
  the Testing section of `CLAUDE.md` (the paragraph explaining why
  `MealDetailViewModelTest` is instrumented becomes obsolete), and add the
  moved tests to CI's coverage implicitly via `testDebugUnitTest`.
- If it fights AGP 9.3 or the Compose BOM, write the findings as a short
  paragraph in `CLAUDE.md` Testing so nobody retries it blind.

### 2.8 Add Dependabot for Gradle  `[Sonnet]`

Add `.github/dependabot.yml` with a weekly `gradle` ecosystem entry for `/`
and a `github-actions` entry. Group all AndroidX updates into one PR.

---

## Phase 3: Additions for reuse and for AI reference

### 3.1 Derivation script  `[Opus]`

Add `scripts/new-project.sh <new.package.name> <AppName>` that:
- moves `app/src/{main,test,androidTest}/java/com/example/scaffold` to the
  new package path and rewrites `package`/`import` lines;
- rewrites `namespace`, `applicationId` in `app/build.gradle.kts`,
  `rootProject.name` in `settings.gradle.kts`, `app_name` in `strings.xml`,
  and the `ScaffoldApp`/`ScaffoldTheme`/`ScaffoldNavHost` class names;
- takes an optional `--strip-samples` flag that deletes the Meal and
  Contact features, their data layer, DI bindings, strings, and tests,
  leaving an empty `NavHost` that still compiles;
- ends by running the definition-of-done Gradle command.
Document usage in `README.md`. Test it by running it into a temp directory.

### 3.2 Committed Claude Code project settings  `[Sonnet]`

- Add `.claude/settings.json` (committed) with a permissions allowlist for
  the Gradle tasks in the definition of done, `git status/diff/log`, and
  `WebFetch` on `developer.android.com` and `mvnrepository.com`.
- Add `.claude/settings.local.json` to `.gitignore`.

### 3.3 Project skills  `[Fable]`

Add two skills under `.claude/skills/`:
- `check/SKILL.md`: runs the definition-of-done command, then
  `connectedDebugAndroidTest` if `git diff --name-only` touches
  `data/local`, `data/remote`, or `data/repository`. Reports failures
  verbatim.
- `new-feature/SKILL.md`: given a feature name and kind (`read` or `form`),
  creates `<Name>UiState.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt`, the
  previews file from 2.1, a `Destinations` route, a `composable<>` entry in
  `ScaffoldNavHost`, and a ViewModel test with a fake repository, all
  following `CLAUDE.md`. Reference the Meals feature as the read template
  and Contact form as the form template.

### 3.4 "Don'ts" section in `CLAUDE.md`  `[Fable]`

Add a short section near the top listing anti-patterns, each with a one-line
why: no `Context`/`Application` in ViewModels; no `runCatching` around
suspend calls; no destructive Room migrations; no user-facing strings in
Kotlin; no `String` exception messages in UiState; no `deleteAll` before a
network fetch; no `Channel`-based one-off events (use the state field from
1.6); no mocking libraries.

### 3.5 Settings tab with DataStore (worked example of "adding a third tab")  `[Opus]`

- Add `androidx.datastore:datastore-preferences`.
- `data/local/UserPreferencesDataSource.kt` exposing
  `observeThemeMode(): Flow<ThemeMode>` and `setThemeMode()`;
  `data/repository/UserPreferencesRepository` over it; bind in
  `RepositoryModule`.
- `ui/feature/settings/` with UiState, ViewModel, Screen (a segmented
  button or radio group for System / Light / Dark).
- Add `Destinations.Settings` and a `TopLevelDestination.Settings` entry.
- `MainActivity` collects the theme mode and passes `darkTheme` into
  `ScaffoldTheme`.
- Tests: ViewModel with a fake repository; DataStore round-trip in
  `androidTest`.
- Update `CLAUDE.md`: the bottom-nav description says two tabs; make it
  three, and add DataStore to the Data layer section as the pattern for
  key-value preferences (vs Room for records).

### 3.6 Pull-to-refresh on the Meals list  `[Opus]`

Wrap `MealList` in `PullToRefreshBox`, driven by a `isRefreshing` field on
`MealListUiState.Content` (set from the refresh status introduced in 1.1).
Refresh failures show via the transient error from 1.6.

### 3.7 Compose screenshot tests  `[Opus]`

Apply AGP's Compose Preview Screenshot Testing plugin
(`com.android.compose.screenshot`), point it at the previews from 2.1,
generate reference images with `updateDebugScreenshotTest`, commit them,
and add `validateDebugScreenshotTest` to CI. Document the update/validate
workflow in `CLAUDE.md` Testing.

### 3.8 Deep link on `MealDetail`  `[Sonnet]`

Add a `navDeepLink<Destinations.MealDetail>(basePath = "scaffold://meal")`
to its `composable<>` entry, the matching `<intent-filter>` in the
manifest, and a `CLAUDE.md` Navigation note. Verify with
`adb shell am start -d "scaffold://meal/52772"`.
