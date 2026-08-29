# Scaffold

A personal base Android project used as a reference template: clone/copy this
structure when starting a new app, and use it as the source of truth for
conventions in this and derived projects.

## Stack

- Kotlin 2.2.10, AGP 9.3.2 (new declarative DSL, built-in Kotlin compilation
  — no separate `org.jetbrains.kotlin.android` plugin), Gradle 9.5.0
- compileSdk / targetSdk 37, minSdk 24
- Jetpack Compose + Material 3
- Hilt (DI), Room (local cache), Retrofit + OkHttp + kotlinx.serialization
  (networking), Navigation Compose with type-safe routes
- detekt + ktlint for static analysis/formatting

Single `:app` module — no multi-module split. Package-by-layer at the top
level (`data`, `di`, `model`, `ui`), package-by-feature inside `ui.feature`.

## Architecture: MVVM + unidirectional data flow

Each feature under `ui/feature/<name>/` has three files:

- `<Name>UiState.kt` — a `sealed interface` with `Loading`, `Error(message)`,
  `Content(...)` cases. No boolean soup (`isLoading`/`error`/`data` all on one
  object) — pick a case, exhaustively `when` over it in the Composable.
- `<Name>ViewModel.kt` — `@HiltViewModel`, exposes a single
  `StateFlow<UiState>`, no other public surface besides user-triggered
  actions (e.g. `refresh()`).
- `<Name>Screen.kt` — `@Composable`, takes a `viewModel: X = hiltViewModel()`
  default param, collects state with `collectAsStateWithLifecycle()`, and
  `when`s over the UI state. Navigation callbacks (`onBack`, `onXClick`) are
  passed in as lambdas from the nav graph, not resolved inside the screen.

Screen-scoped nav arguments are read by the ViewModel via
`SavedStateHandle.toRoute<Destinations.X>()`, not passed as Composable
parameters — see `MealDetailViewModel` for the pattern.

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
source, just a `suspend fun saveContact()` write path into Room — the
counterpart to `MealRepository`'s read path.

**Schema changes get an explicit `Migration`, never destructive fallback.**
Bump `@Database(version = ...)` in `AppDatabase` and add a `Migration`
object in `data/local/Migrations.kt` (registered via `.addMigrations(...)`
in `DatabaseModule`). The database is currently at version 1 with no
migration history — `Migrations.kt` doesn't exist yet — so the first real
schema change after this baseline is what creates that file and starts
numbering from `MIGRATION_1_2`.

## Dependency injection

Hilt modules live in `di/`, one per concern (`NetworkModule`,
`DatabaseModule`, `RepositoryModule`). Bind interfaces to implementations
with `@Binds` in an `abstract class` module; provide third-party types
(`Retrofit`, `AppDatabase`, `OkHttpClient`) with `@Provides` in an `object`
module. Everything is `@Singleton` — this app has no scoped/per-screen
dependencies yet.

## Navigation

`ui/navigation/Destinations.kt` defines routes as a `sealed interface` of
`@Serializable` objects/data classes. `ScaffoldNavHost.kt` wires them to
screens with the type-safe `composable<Destinations.X>` overload — no string
routes, no manual argument bundling.

The app is a 2-tab bottom-nav app (Contacts, Meals). `TopLevelDestination.kt`
enumerates the tabs (route + label + icon); `ScaffoldNavHost` holds a single
`NavController` and outer `Scaffold`/`NavigationBar` shared by both tabs —
there's no per-tab back stack or nested `NavHost`. Tab switches use the
standard `popUpTo(graph.findStartDestination().id) { saveState = true }` +
`launchSingleTop` + `restoreState` combo so each tab keeps its own scroll
position/state when you switch away and back. The bottom bar itself is
hidden on non-top-level destinations (`MealDetail`, `ContactForm`) via
`currentDestination.hierarchy.any { it.hasRoute(topLevel.route::class) }` —
detail/form screens are full-screen, not tab content.

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

A ViewModel that reads `SavedStateHandle.toRoute<Destinations.X>()` (any
`<Name>DetailViewModel`) can't be unit-tested as a plain JVM test either —
`toRoute()` internally calls `androidx.core.os.BundleKt.bundleOf`, and
`android.os.Bundle` is stubbed to throw ("not mocked") outside a real
Android runtime. `MealDetailViewModelTest` lives in `app/src/androidTest/`
for this reason, alongside `MealRepositoryEndToEndTest`, and sets
`Dispatchers.Main` to an `UnconfinedTestDispatcher` itself in `@Before`/
`@After` (the same thing `MainDispatcherRule` does for JVM tests) since a
real device's `Dispatchers.Main` is the actual main-looper dispatcher, not
something `advanceUntilIdle()` can drive on its own.

Compose UI tests (`MealListScreenTest`) don't need Hilt test infrastructure:
every `<Name>Screen` already accepts an explicit `viewModel` parameter
(default `= hiltViewModel()`), so a test can construct a real ViewModel
directly against a hand-written fake repository — the same fakes used in
the ViewModel unit tests — and pass it straight in, exercising the actual
Composable through Compose's real rendering/click handling with no DI
involved.

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
- A sealed `Error(message: String?)` case that can carry either a live
  exception's message *or* a static fallback stays nullable all the way
  through the ViewModel (`MealListUiState.Error(throwable.message)`, no
  `?:` in the ViewModel) — the Composable supplies the fallback:
  `state.message ?: stringResource(R.string.meal_list_error_fallback)`.
  This is the one place a `String?` (not a resource id) flows out of a
  ViewModel, because the live exception text is inherently dynamic content
  that can never itself be a resource.
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
`.github/workflows/ci.yml` runs `ktlintCheck detekt testDebugUnitTest
assembleDebug` on every push/PR to `main`, but that's a remote safety net,
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
