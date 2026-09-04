---
name: check
description: Run this project's full verification suite — ktlintFormat, ktlintCheck, detekt, lintDebug, testDebugUnitTest, assembleDebug, plus connectedDebugAndroidTest when the change touches the data layer. Use before committing, when asked to "check", "verify", or "run the checks", and after finishing any code change in this repo.
---

# Check

The definition of done for any change in this repo. Report failures verbatim —
never summarise a compiler or lint error into your own words, and never call a
change done while any step below is failing.

## 1. Always run the JVM suite

There is no standalone JDK on this machine, so `JAVA_HOME` must be set:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew ktlintFormat ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
```

`ktlintFormat` runs first and rewrites files; if it changed anything, those
edits are part of the change and need committing too.

Common failures and what they mean:

- `[MaxLineLength]` / `[LongMethod]` from detekt — extract a function rather
  than reflowing into something unreadable; 80 lines is the method limit.
- `[UnusedPrivateMember]` on a `@Preview` — check `config/detekt/detekt.yml`
  still lists the annotation under `ignoreAnnotated`.
- Android Lint failures have no baseline by design. Fix them; don't add one.

## 2. Run the instrumented suite when the data layer changed

```sh
git diff --name-only HEAD
```

If any changed path is under `data/local`, `data/remote`, or `data/repository`
(including their tests), also run:

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew connectedDebugAndroidTest
```

It needs a booted device. This project's AVD is `Pixel_9`:

```sh
~/Library/Android/sdk/emulator/emulator -avd Pixel_9 &
~/Library/Android/sdk/platform-tools/adb wait-for-device shell \
  'while [ -z "$(getprop sys.boot_completed)" ]; do sleep 2; done'
```

Note that `connectedDebugAndroidTest` uninstalls the app afterwards, so
re-run `./gradlew installDebug` if you were also poking at the app by hand.

## 3. Report

Say which steps ran and their result. If step 2 was skipped, say so and why
("no data-layer files changed"). If anything failed, paste the failing output
and stop — don't attempt a fix the user hasn't asked for.
