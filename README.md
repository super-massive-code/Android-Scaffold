# Scaffold

[![CI](https://github.com/super-massive-code/Android-Scaffold/actions/workflows/ci.yml/badge.svg)](https://github.com/super-massive-code/Android-Scaffold/actions/workflows/ci.yml)

A personal base Android project used as a reference template — clone or copy
this structure when starting a new app, and treat it as the source of truth
for conventions in derived projects.

It's a small two-tab app (Contacts, Meals) that exercises the full stack end
to end: a local-only CRUD feature and a network-backed, offline-first
feature, both wired through the same architecture.

## Stack

- Kotlin 2.2.10, AGP 9.3.2 (declarative DSL, built-in Kotlin compilation),
  Gradle 9.5.0
- compileSdk / targetSdk 37, minSdk 24
- Jetpack Compose + Material 3
- Hilt (DI), Room (local cache), Retrofit + OkHttp + kotlinx.serialization
  (networking), Navigation Compose with type-safe routes
- detekt + ktlint + Android Lint for static analysis/formatting

Single `:app` module — package-by-layer at the top level (`data`, `di`,
`model`, `ui`), package-by-feature inside `ui.feature`. See
[`CLAUDE.md`](CLAUDE.md) for the full set of architectural conventions this
project follows (MVVM + unidirectional data flow, the offline-first
repository pattern, string-resource discipline, testing strategy, etc.).

## Building

Requires a JDK (e.g. the one bundled with Android Studio):

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

## Testing

```sh
./gradlew testDebugUnitTest          # JVM unit tests (ViewModels, validation)
./gradlew connectedDebugAndroidTest  # instrumented tests, needs a device/emulator
```

## Static analysis

```sh
./gradlew ktlintCheck
./gradlew detekt
./gradlew lintDebug   # Android Lint, warningsAsErrors
```

CI (`.github/workflows/ci.yml`) runs `ktlintCheck detekt lintDebug
testDebugUnitTest assembleDebug` on every push/PR to `main`.
