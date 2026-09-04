# Scaffold

A personal base Android project used as a reference template: clone/copy this
structure when starting a new app, and use it as the source of truth for
conventions in this and derived projects.

## Don'ts

Each of these has bitten this project or a derived one. The rest of the
document explains the alternatives; this list is what not to do.

- **No `Context`/`Application` in a ViewModel.** It exists to resolve
  strings, and that's the Composable's job — carry a `@StringRes Int`
  instead. See Strings.
- **No user-facing text in Kotlin source.** Every string a user can read
  lives in `res/values/strings.xml`, including content descriptions and tab
  labels. Text assembled from parts uses a format resource, never a Kotlin
  template. See Strings.
- **No exception `message` in a UiState.** Map it to a `UiError` so the user
  sees localised, actionable text rather than "Unable to resolve host". See
  Strings.
- **No `runCatching` around a suspend call.** It swallows
  `CancellationException` and breaks structured concurrency — use
  `runSuspendCatching` from `util/`. See Architecture.
- **No `Channel`/`SharedFlow` of one-off UI events.** A nullable field on
  `Content` that the screen acknowledges survives rotation and is assertable
  from `uiState.value`. See Architecture.
- **No clearing the cache before a network fetch.** Fetch first, then
  replace in one transaction, or a failed request leaves the user with an
  empty screen — the opposite of what an offline-first repository is for.
  See Data layer.
- **No destructive Room migrations.** Every schema change gets an explicit
  `Migration`, a committed schema JSON, and a `MigrationTest` case.
  `fallbackToDestructiveMigration()` deletes real users' data. See Data
  layer.
- **No `Dispatchers.IO` outside `CoroutinesModule`.** Repositories take an
  injected `@IoDispatcher` so tests can substitute one. See Dependency
  injection.
- **No mocking libraries.** Hand-write a fake implementing the interface;
  where a fake would hide a real integration failure (a mapper dropping a
  primary key, a wrong `@Query`), write an end-to-end test against real Room
  instead. See Testing.

## Stack

- Kotlin 2.2.10, AGP 9.3.2 (new declarative DSL, built-in Kotlin compilation
  — no separate `org.jetbrains.kotlin.android` plugin), Gradle 9.5.0
- compileSdk / targetSdk 37, minSdk 24
- Jetpack Compose + Material 3
- Hilt (DI), Room (local cache), Retrofit + OkHttp + kotlinx.serialization
  (networking), Navigation Compose with type-safe routes
- detekt + ktlint + Android Lint for static analysis/formatting

Single `:app` module — no multi-module split. Package-by-layer at the top
level (`data`, `di`, `model`, `ui`, `util`), package-by-feature inside
`ui.feature`.

## Architecture: MVVM + unidirectional data flow

Each feature under `ui/feature/<name>/` has three files:

- `<Name>UiState.kt` — a `sealed interface` with `Loading`, `Error(message)`,
  `Content(...)` cases. No boolean soup (`isLoading`/`error`/`data` all on one
  object) — pick a case, exhaustively `when` over it in the Composable.
- `<Name>ViewModel.kt` — `@HiltViewModel`, exposes a single
  `StateFlow<UiState>`, no other public surface besides user-triggered
  actions (e.g. `refresh()`).
- `<Name>Screen.kt` — **two public overloads of the same name.** The
  *stateful* one takes `viewModel: X = hiltViewModel()`, collects state with
  `collectAsStateWithLifecycle()`, and delegates to the *stateless* one,
  which takes `uiState: XUiState` plus a lambda per user action and `when`s
  over the state. Navigation callbacks (`onBack`, `onXClick`) are passed in
  as lambdas from the nav graph, not resolved inside the screen.
- `<Name>ScreenPreviews.kt` — one `@PreviewLightDark` per UiState case,
  wrapped in `ScaffoldTheme`, calling the stateless overload.

A ViewModel that wraps a repository call for its error path uses
`runSuspendCatching` (`util/RunSuspendCatching.kt`), never `runCatching`:
the former rethrows `CancellationException` so a superseded refresh (the
`refreshJob` a new `selectCategory` cancels) doesn't surface as an error the
user sees. Anything else — `IOException`, `HttpException`, a serialization
failure — comes back as a `Result.failure` to be mapped into the UI state.

**The stateless overload is the unit of preview and test; the stateful one
is the unit of navigation.** Only the stateful wrapper knows a ViewModel
exists (and owns effects that navigate, like the form's
`LaunchedEffect(uiState.isSubmitted) { onBack() }`), so everything below it
is reachable from a `@Preview` and from a Compose UI test with a literal
UiState — no Hilt, no fake repository, no coroutines. Anything the body
needs from the ViewModel arrives as a parameter: state in, callbacks out.

Screen-scoped nav arguments are read by the ViewModel via
`SavedStateHandle.toRoute<Destinations.X>()`, not passed as Composable
parameters — see `MealDetailViewModel` for the pattern.

**One-off UI feedback is a field on `Content`, not an event stream.** When a
refresh fails while there's already something on screen, replacing the
content with an error screen would be a downgrade — the cached list is still
useful. So `Content` carries a `transientError: UiError?` alongside its data,
the screen shows it in a `SnackbarHost` from a
`LaunchedEffect(state.transientError)`, and calls the ViewModel's
`dismissTransientError()` once `showSnackbar` returns. Failures with nothing
to fall back on still become the `Error` case.

Deliberately *not* a `Channel`/`SharedFlow` of one-off events: state
survives rotation (a `Channel` emission delivered while the screen is being
recreated is simply lost), it's assertable from `uiState.value` in a plain
unit test with no collector, and there's exactly one place a screen reads
its state from. The cost is that the ViewModel must be told when the message
has been consumed, which is what `dismissTransientError()` is for — see
`MealListViewModel`/`MealDetailViewModel`.

A destructive action follows the same transient-feedback shape, one step
further: `ContactListUiState.Content` carries `recentlyDeleted: Contact?`,
the screen shows a snackbar with an Undo action, and the ViewModel's
`undoDelete()` re-inserts the contact under its original id (`dismissUndo()`
for the dismissed case). The delete happens immediately — an undo that
re-inserts is simpler and more honest than a pending-delete timer, and it
behaves correctly if the process dies mid-snackbar.

Two things about `SwipeToDismissBox` in a keyed `LazyColumn`, both learned
the hard way in `ContactListScreen`: `rememberSwipeToDismissBoxState()` is
saved *per item key*, so a row that comes back (undo re-inserting it under
the same id) is rebuilt with its state still at `EndToStart` — snap it back
to `Settled` on composition, or the row renders permanently swiped away. And
the delete must fire on a *transition* into that anchor
(`snapshotFlow { state.currentValue }.drop(1).filter { … }`), never on
`currentValue` read directly, or the restored row deletes itself again the
instant undo puts it back.

**Forms are the exception to the sealed-interface rule.** A form has no
resource-loading lifecycle, so `<Name>UiState` is a flat `data class` of
field values, per-field `@StringRes Int?` errors, and an
`isSubmitting`/`isSubmitted` pair instead — see `ContactFormUiState`. Field
changes are individual ViewModel methods (`onFirstNameChange(value)`, one per
field) that clear that field's error; `submit()` validates everything at
once, writes the errors back into state, and bails out if any are non-null
before calling the repository. The screen navigates back via a
`LaunchedEffect(uiState.isSubmitted)`, not a callback threaded through the
ViewModel.

The write itself can fail too, so the same state carries a
`@StringRes submitError: Int?`: `submit()` clears it as part of validation,
`runSuspendCatching` around the repository call sets it (and puts
`isSubmitting` back to `false`) on failure, and the screen renders it as a
`Text` in `MaterialTheme.colorScheme.error` above the Save button. It's a
`@StringRes`, not the `UiError` a read screen uses, because a local write
has one thing to say and no exception detail worth mapping — the form stays
on screen with the user's input intact so they can retry.

Form validation itself lives in a co-located `<Name>FormValidation.kt` —
top-level pure functions (not a wrapper object; no shared state to justify
one, same style as `MealMappers.kt`/`ContactMappers.kt`) that take a raw
field value and return `@StringRes Int?`, e.g. `requiredFieldError(value)`,
`postcodeError(value)` in `ContactFormValidation.kt`. The ViewModel never
resolves string resources itself (no injected `Context`/`Application`) — the
Composable resolves the id with `stringResource()` only when rendering
`supportingText`. This keeps validation rules unit-testable in plain JUnit
with no `MainDispatcherRule`/coroutines/Android context involved — see
`ContactFormValidationTest`.

## Data layer

Offline-first repository pattern:

- `data/remote/` — Retrofit `interface` + `@Serializable` DTOs.
- `data/local/` — Room `@Entity` + `@Dao`, exposing `Flow` queries.
- `data/repository/` — a `Repository` interface with `observeX(): Flow<X>`
  (reads from Room only) and `suspend fun refresh()` (fetches from the
  network, writes into Room). The UI layer never touches Retrofit or DTOs
  directly — only the domain model in `model/`.
- Mappers (`toEntity()`, `toDomain()`) live next to the repository impl, not
  on the model classes themselves.

See `MealRepositoryImpl` for the reference implementation, backed by
`themealdb.com` as a placeholder API. Its `refresh()` fetches the list (id,
title, thumbnail) from `filter.php`; because that endpoint doesn't include
the full recipe text, a second `refresh(id: String)` lazily fetches one
meal's full detail from `lookup.php` when its detail screen opens — a
repository is free to add such a per-item refresh overload alongside the
list-level one where the backing API's list/detail payloads genuinely
differ in shape, rather than forcing a single `refresh()` to over-fetch.
`ContactRepository` is the local-only variant of the same pattern: no remote
source, just `saveContact`/`updateContact`/`deleteContact` write paths into
Room alongside `observeContacts()`/`observeContact(id)` — the counterpart to
`MealRepository`'s read path. Note `Contact.toEntity()` carries the `id`
through: `@Update` and `@Delete` match on the primary key, and dropping it
makes both silently no-op. That class of mapper bug is invisible to a fake
DAO, which is why `ContactRepositoryEndToEndTest` drives the real Room
database the same way `MealRepositoryEndToEndTest` does. It also owns `seedIfEmpty()`,
called once from `ScaffoldApp.onCreate()`, so a fresh install shows the
Contacts tab with something in it rather than the empty state — any
"ensure the store has starting data" logic belongs behind the repository
interface like this, not written inline in `ScaffoldApp`/`MainActivity`.

**Per-environment configuration lives in `buildConfigField`, never a Kotlin
constant.** The API base URL is declared in `defaultConfig` and read as
`BuildConfig.MEAL_API_BASE_URL` in `NetworkModule`, so a build type or
flavour can point the app at staging by overriding one line of Gradle
instead of editing source. Values that are the same everywhere (the OkHttp
`connectTimeout`/`readTimeout`) stay as named Kotlin constants.

**Schema changes get an explicit `Migration`, never destructive fallback.**
Bump `@Database(version = ...)` in `AppDatabase` and add a `Migration`
object in `data/local/Migrations.kt` (registered via `.addMigrations(...)`
in `DatabaseModule`). The database is currently at version 1 with no
migration history — `Migrations.kt` doesn't exist yet — so the first real
schema change after this baseline is what creates that file and starts
numbering from `MIGRATION_1_2`.

Schemas are exported (`exportSchema = true`, `room { schemaDirectory(...) }`
via the `androidx.room` Gradle plugin) to `app/schemas/`, and **the generated
`<version>.json` is committed with the change that produced it** — it's the
only record of what the old schema was, and `MigrationTestHelper` needs it to
create a database at an earlier version. `app/src/androidTest/.../data/local/
MigrationTest.kt` is the template: it currently just asserts version 1 matches
the compiled database, and each new migration adds a case that creates the old
version, writes a row, runs `runMigrationsAndValidate`, and asserts the row
survived.

## Dependency injection

Hilt modules live in `di/`, one per concern (`NetworkModule`,
`DatabaseModule`, `RepositoryModule`, `CoroutinesModule`). Bind interfaces to implementations
with `@Binds` in an `abstract class` module; provide third-party types
(`Retrofit`, `AppDatabase`, `OkHttpClient`) with `@Provides` in an `object`
module. Everything is `@Singleton` — this app has no scoped/per-screen
dependencies yet.

**Dispatchers are injected, never referenced directly.**
`CoroutinesModule` provides `@IoDispatcher CoroutineDispatcher`
(`Dispatchers.IO`) and `@ApplicationScope CoroutineScope` (a `SupervisorJob`
on that dispatcher), both `@Qualifier` annotations declared in the same
file. Every repository takes `@param:IoDispatcher private val ioDispatcher`
and wraps its suspend bodies in `withContext(ioDispatcher)` — so callers
(ViewModels, `MealRepositoryEndToEndTest`) never have to know or care which
thread a repository call blocks on, and a test can substitute
`UnconfinedTestDispatcher()`. A `Dispatchers.IO` literal anywhere outside
`CoroutinesModule` is a bug.

Process-lifetime work injects `@ApplicationScope` rather than building its
own scope: `ScaffoldApp.onCreate()` launches `contactRepository.seedIfEmpty()`
into it. Seeding deliberately stays behind the repository interface rather
than moving into a `RoomDatabase.Callback.onCreate` in `DatabaseModule` —
the callback would need a `Provider<ContactDao>` to break the cycle back to
the database it's constructing, and it would put a piece of app behaviour in
a DI module where nobody thinks to look for it. "Ensure the store has
starting data" is a repository concern; *when* to run it is the app's.

## Navigation

`ui/navigation/Destinations.kt` defines routes as a `sealed interface` of
`@Serializable` objects/data classes. `ScaffoldNavHost.kt` wires them to
screens with the type-safe `composable<Destinations.X>` overload — no string
routes, no manual argument bundling.

The app is a 2-tab bottom-nav app (Contacts, Meals). `TopLevelDestination.kt`
enumerates the tabs (route + label + icon); `ScaffoldNavHost` holds a single
`NavController` and a `NavigationBar` shared by both tabs — there's no
per-tab back stack or nested `NavHost`. There's deliberately no outer
`Scaffold`: the bar is overlaid on top of the `NavHost` inside a `Box` (with
`AnimatedVisibility` to fade it in/out) rather than resizing the content
area around it, so `NavHost` always fills the screen and a screen transition
(e.g. the meal list/detail shared element) never has its coordinate space
perturbed by the bar appearing/disappearing. Tab switches use the standard
`popUpTo(graph.findStartDestination().id) { saveState = true }` +
`launchSingleTop` + `restoreState` combo so each tab keeps its own scroll
position/state when you switch away and back. The bottom bar itself is
hidden on non-top-level destinations (`MealDetail`, `ContactForm`) via
`currentDestination.hierarchy.any { it.hasRoute(topLevel.route::class) }` —
detail/form screens are full-screen, not tab content.

An optional screen argument is a **sentinel, not a nullable primitive**:
`Destinations.ContactForm(val contactId: Long = NEW_CONTACT)` where
`NEW_CONTACT` is `0L`. Type-safe routes have no `NavType` for `Long?`, and 0
is unambiguous here because Room's `autoGenerate` ids start at 1 — it's
already what `Contact.id` holds before a row is written. The form reads it
with `SavedStateHandle.toRoute()` like any other screen argument and decides
between insert and update from that one value.

Adding a third tab: add its route to `Destinations`, add an entry to
`TopLevelDestination`, add its `composable<...>` to the `NavHost` — the bottom
bar updates automatically since it iterates `TopLevelDestination.entries`.

## Testing

Prefer hand-written fakes over mocking libraries: implement the repository
interface directly with an in-memory `MutableStateFlow` (see
`MealListViewModelTest`, which fakes `MealRepository`). ViewModel tests need
`MainDispatcherRule` (`app/src/test/java/com/example/scaffold/MainDispatcherRule.kt`)
to give `viewModelScope` a `TestDispatcher` as `Dispatchers.Main`.

Repository-level correctness — real JSON parsing, real Retrofit routing,
real Room persistence — is covered by `MealRepositoryEndToEndTest`
(`app/src/androidTest/...`) rather than a `MealApi`/`MealDao`-faking unit
test. It wires up a real Retrofit client (the app's real `Json` config, real
kotlinx.serialization converter) against a local `mockwebserver3.MockWebServer`
serving canned copies of themealdb.com's actual responses, and a real
in-memory Room database (`Room.inMemoryDatabaseBuilder`) — only the socket
themealdb.com sits behind is substituted, everything else is the genuine
`MealRepositoryImpl`. Don't add a fakes-based repository test alongside this
one for the same scenarios — that duplication was tried and cut; it added
maintenance cost with no added confidence, since this test already covers
everything the fakes did plus real-wiring failure modes they structurally
can't (a wrong `@Query` name, TheMealDB's real `{"meals":null}` response).
It needs a connected device or emulator: `./gradlew
connectedDebugAndroidTest`.

**Everything that needs an Android runtime but not a real device runs on the
JVM under Robolectric.** That covers two things a plain JVM test can't do:
a ViewModel reading `SavedStateHandle.toRoute<Destinations.X>()` (`toRoute()`
goes through `androidx.core.os.BundleKt.bundleOf`, and `android.os.Bundle`
is stubbed to throw "not mocked" without one), and Compose UI tests. So
`MealDetailViewModelTest`, `ContactFormViewModelTest`, `MealListScreenTest`
and `ContactListScreenTest` all live in `app/src/test/` with
`@RunWith(RobolectricTestRunner::class)` — plus
`@GraphicsMode(GraphicsMode.Mode.NATIVE)` on the two Compose ones — and use
the same `MainDispatcherRule` as every other JVM test. The whole JVM suite,
Robolectric included, runs in about 10 seconds, and CI covers it via
`testDebugUnitTest` with no emulator.

Two pieces of setup make that work: `testOptions { unitTests
.isIncludeAndroidResources = true }` in `app/build.gradle.kts`, and
`app/src/test/resources/robolectric.properties` pinning `sdk=35` — Robolectric
4.15.1 has no android-all jar for `compileSdk` 37, and without the pin every
Robolectric test fails at startup. Raise that pin when Robolectric catches up.

What stays in `androidTest`: the data-layer suites
(`MealRepositoryEndToEndTest`, `ContactRepositoryEndToEndTest`,
`MigrationTest`). Those exist to prove real Room/SQLite behaviour on a real
Android runtime, which is exactly what Robolectric would be substituting.

Independently of where a test runs: push logic that needs no Android runtime
into pure functions and unit test it there. `ContactFormValidationTest` has
by far the most cases of any test in the project and needs no runtime at all,
because the rules are top-level functions over a `String`.

**Compose UI tests can't assert past a list item being removed** on this
toolchain: `waitForIdle()` after an item leaves a `LazyColumn` never returns
("ComposeIdlingResource is busy due to pending measure/layout"), and it
reproduces with a bare `LazyColumn` of `Text`s, so it isn't something this
app's screens cause. The running app is unaffected (idle at 0% CPU after a
delete). Test callbacks and states that don't remove a row, and cover the
removal itself in the ViewModel and repository tests, which have no such
limitation.

Compose UI tests (`MealListScreenTest`) drive the stateless `<Name>Screen`
overload directly with a literal UiState and record what its callbacks were
handed. No Hilt, no ViewModel, no fake repository — the Composable is
exercised through Compose's real rendering/click handling, and what the
ViewModel does in response to those callbacks is the ViewModel test's
subject, not this one's.

`ErrorState` (`ui/components/StatusComposables.kt`) takes a nullable
`onRetry: (() -> Unit)? = null` and only renders the Retry button when it's
non-null. Read screens backed by a network refresh (`MealList`, `MealDetail`)
pass a real retry action; screens backed by a pure local `Flow` with nothing
to retry (`ContactList`) omit it rather than wiring up a button that lies
about doing something.

## Strings

**No hardcoded user-facing text in Kotlin source — every string a user can
see lives in `res/values/strings.xml`.** This covers screen titles, field
labels, button text, content descriptions, tab labels, and error messages —
not just obviously-"content" text. Debug-only strings (log tags, exception
messages thrown internally) are exempt.

- Composables resolve resources directly with `stringResource(R.string.x)`
  at the point of use — don't pre-resolve to a `String` higher up and pass
  that down, so the string stays swappable per-locale as low as possible.
- Something that needs to reference a string but *isn't* itself a
  `@Composable` (an `enum`, a `ViewModel`) carries a `@StringRes Int`
  instead, and whichever Composable renders it calls `stringResource()` —
  see `TopLevelDestination.label` (an enum, evaluated outside Composition,
  can't call `stringResource()` itself) and `ContactFormUiState`'s per-field
  errors. A ViewModel never injects `Context`/`Application` just to resolve
  a string.
- **No exception text ever reaches the UI.** A sealed `Error` case carries
  a `UiError` (`ui/components/UiError.kt`) — `Network`, `Server` or
  `Unknown`, each with a `@StringRes messageRes` — never a `String`
  message. ViewModels map with `throwable.toUiError()`
  (`IOException` -> `Network`, `HttpException` -> `Server`, else
  `Unknown`), and the Composable renders
  `stringResource(state.error.messageRes)`. "Unable to resolve host
  www.themealdb.com" is a log line: untranslatable, and not something a
  user can act on. A new failure worth its own wording gets a new `UiError`
  case and its own string, not a `String` field.
- **User-visible text assembled from parts uses a format resource**, never a
  Kotlin string template: `stringResource(R.string.contact_list_full_name,
  first, last)`, not `"$first $last"`. Word order, separators and spacing
  differ by locale, and a template hard-codes English ones into Kotlin where
  no translator can reach them.
- Name resources `<feature>_<kind>_<descriptor>`
  (`contact_form_label_first_name`, `contact_form_error_required`). Reuse
  one resource for identical text used for the same concept in more than
  one place — e.g. `nav_contacts` backs both the bottom-nav tab label and
  the Contacts screen's app-bar title, and `action_back`/`action_retry`
  are shared across every screen with a back button or retry action —
  rather than duplicating the same English string under different names.

## Static analysis

- `./gradlew detekt` — config at `config/detekt/detekt.yml`, builds on
  detekt's default ruleset. `MagicNumber` ignores `@Composable` functions
  (Compose dimension literals like `16.dp` aren't a smell).
- `./gradlew ktlintCheck` / `ktlintFormat` — Android Kotlin style guide
  enabled (`android.set(true)`).
- `./gradlew lintDebug` — Android Lint with `abortOnError` and
  `warningsAsErrors`, and **no baseline**: a lint warning is fixed, not
  parked. The three dependency-freshness checks
  (`AndroidGradlePluginVersion`, `GradleDependency`, `NewerVersionAvailable`)
  are disabled in `app/build.gradle.kts` — they turn any upstream release
  into a red build, and keeping dependencies current is Dependabot's job.

**`.editorconfig` (project root) is load-bearing, not cosmetic.** Without
`max_line_length = 120`, ktlint has no line-length limit of its own and will
fight detekt's 120-char `MaxLineLength` — collapsing a wrapped expression
back onto one line, which detekt then re-flags, forever. Without
`ktlint_function_naming_ignore_when_annotated_with = Composable`, ktlint's
naming rule doesn't know `@Composable` functions are conventionally
PascalCase (detekt's `FunctionNaming.ignoreAnnotated` in `detekt.yml` only
covers detekt's own check) and will flag every screen composable in the app.
Both were only discovered by actually running `ktlintCheck`/`detekt` —
they're configured via Gradle at setup time, and nothing runs them locally
on its own, so don't assume a clean tree just because the project builds.
`.github/workflows/ci.yml` runs `ktlintCheck detekt lintDebug
testDebugUnitTest assembleDebug` on every push/PR to `main`, but that's a remote safety net,
not a substitute for running them yourself before committing. CI
deliberately excludes `connectedDebugAndroidTest` — the `androidTest` suite
needs a booted emulator/device (see `./gradlew connectedDebugAndroidTest`
and this project's `Pixel_9` AVD), which is heavier to provision in CI than
it's worth for a personal scaffold; run it locally when touching
`MealRepositoryImpl` or its collaborators.

## Release builds

`optimization { enable = true }` in the release `buildType` is AGP 9.3's
unified replacement for `minifyEnabled`/`shrinkResources` — one flag turns on
R8 shrinking, obfuscation, and resource shrinking together, plus a default
keep-rules file equivalent to `proguard-android-optimize.txt`. Custom keep
rules go in `.keep` files under `src/<variant>/keepRules/`, not a
`proguard-rules.pro`. Room/Retrofit/OkHttp/Hilt/kotlinx.serialization all
ship their own consumer keep rules, so none of our own are needed for the
current, non-reflective usage.

## Environment gotcha

There's no standalone JDK on this machine — Gradle needs
`JAVA_HOME=/Applications/Android Studio.app/Contents/jbr/Contents/Home` (or
whichever IDE is installed) when running `./gradlew` outside Android Studio.

## Git commits

Keep commit messages terse: a one-line summary, optionally a short bulleted
list for genuinely distinct changes — no multi-paragraph prose, no restating
the diff line by line. State *what* changed in a few words per item; only
add *why* when it isn't obvious from the change itself.

## Adding a new feature

1. If it needs network/local data: add a DTO + Retrofit method, an
   `@Entity` + `@Dao` method, a domain model, and extend/add a repository
   with `observeX()` + `refresh()`.
2. Wire any new provider into the relevant `di/` module.
3. Add `<Name>UiState`, `<Name>ViewModel`, `<Name>Screen` under
   `ui/feature/<name>/`.
4. Add a route to `Destinations` and a `composable<...>` entry in
   `ScaffoldNavHost`.
5. Add repository/ViewModel tests following the fake-based pattern above.
