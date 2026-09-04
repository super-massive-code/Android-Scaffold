#!/usr/bin/env bash
#
# Turn a copy of this scaffold into a new project.
#
#   scripts/new-project.sh <new.package.name> <AppName> [--strip-samples]
#
# Run it inside a fresh copy/clone of the scaffold: it rewrites the checkout in
# place, and refuses to start if the working tree is dirty so that `git diff`
# shows exactly what it did and `git checkout .` undoes it.
set -euo pipefail

usage() {
    cat <<'USAGE'
Usage: scripts/new-project.sh <new.package.name> <AppName> [--strip-samples]

  <new.package.name>  e.g. com.acme.notes  (lowercase, at least two segments)
  <AppName>           e.g. Notes           (PascalCase; names the Application,
                                            theme, nav host and app label)
  --strip-samples     delete the Meals and Contacts features, their data layer,
                      DI bindings, strings and tests, leaving the Settings tab
                      as the only screen — a shell that still builds and runs

Run this in a fresh copy of the scaffold, with a clean git working tree.
USAGE
}

if [[ $# -lt 2 ]]; then
    usage >&2
    exit 2
fi

NEW_PACKAGE="$1"
APP_NAME="$2"
STRIP_SAMPLES=false
shift 2
while [[ $# -gt 0 ]]; do
    case "$1" in
        --strip-samples) STRIP_SAMPLES=true ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage >&2; exit 2 ;;
    esac
    shift
done

if ! [[ "$NEW_PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
    echo "Not a valid package name: $NEW_PACKAGE" >&2
    exit 2
fi
if ! [[ "$APP_NAME" =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
    echo "App name must be PascalCase: $APP_NAME" >&2
    exit 2
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

if [[ -n "$(git status --porcelain 2>/dev/null)" ]]; then
    echo "Working tree is dirty. Commit or stash first — this script rewrites files in place." >&2
    exit 1
fi

OLD_PACKAGE="com.example.scaffold"
OLD_PATH="${OLD_PACKAGE//.//}"
NEW_PATH="${NEW_PACKAGE//.//}"
APP_NAME_LOWER="$(echo "$APP_NAME" | tr '[:upper:]' '[:lower:]')"

echo "Package : $OLD_PACKAGE -> $NEW_PACKAGE"
echo "App name: Scaffold -> $APP_NAME"
echo "Strip samples: $STRIP_SAMPLES"
echo

# ---------------------------------------------------------------- source moves
for source_set in main test androidTest; do
    old_dir="app/src/$source_set/java/$OLD_PATH"
    new_dir="app/src/$source_set/java/$NEW_PATH"
    [[ -d "$old_dir" ]] || continue
    mkdir -p "$(dirname "$new_dir")"
    git mv "$old_dir" "$new_dir" 2>/dev/null || mv "$old_dir" "$new_dir"
    # Drop the now-empty com/example husk.
    find "app/src/$source_set/java" -type d -empty -delete
done

# ------------------------------------------------------------------- rewriting
# Every text file that can mention the package, a Scaffold* name, or the app id.
rewrite_targets() {
    find app/src scripts config .github -type f \
        \( -name '*.kt' -o -name '*.kts' -o -name '*.xml' -o -name '*.keep' -o -name '*.pro' \) 2>/dev/null
    ls app/build.gradle.kts settings.gradle.kts README.md CLAUDE.md 2>/dev/null
}

# shellcheck disable=SC2016
rewrite_targets | sort -u | while read -r file; do
    perl -pi -e "
        s/\Q$OLD_PACKAGE\E/$NEW_PACKAGE/g;
        s/\bScaffoldApp\b/${APP_NAME}App/g;
        s/\bScaffoldTheme\b/${APP_NAME}Theme/g;
        s/\bScaffoldNavHost\b/${APP_NAME}NavHost/g;
        s/\bTheme\.Scaffold\b/Theme.$APP_NAME/g;
        s/\bscaffold\.db\b/$APP_NAME_LOWER.db/g;
        s|\bscaffold://|$APP_NAME_LOWER://|g;
        s/android:scheme=\"scaffold\"/android:scheme=\"$APP_NAME_LOWER\"/g;
    " "$file"
done

# Files named after the classes they hold.
rename_file() {
    [[ -f "$1" ]] || return 0
    git mv "$1" "$2" 2>/dev/null || mv "$1" "$2"
}
MAIN="app/src/main/java/$NEW_PATH"
rename_file "$MAIN/ScaffoldApp.kt" "$MAIN/${APP_NAME}App.kt"
rename_file "$MAIN/ui/navigation/ScaffoldNavHost.kt" "$MAIN/ui/navigation/${APP_NAME}NavHost.kt"

# Project-level names that aren't code identifiers.
perl -pi -e "s/^rootProject\.name = .*/rootProject.name = \"$APP_NAME\"/" settings.gradle.kts
perl -pi -e "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">$APP_NAME</string>|" \
    app/src/main/res/values/strings.xml
perl -pi -e "s/^# Scaffold$/# $APP_NAME/" README.md CLAUDE.md

# --------------------------------------------------------------- strip samples
if [[ "$STRIP_SAMPLES" == true ]]; then
    rm -rf \
        "$MAIN/ui/feature/meallist" \
        "$MAIN/ui/feature/mealdetail" \
        "$MAIN/ui/feature/contactlist" \
        "$MAIN/ui/feature/contactform" \
        "$MAIN/data/remote" \
        "$MAIN/data/local/AppDatabase.kt" \
        "$MAIN/data/local/MealDao.kt" \
        "$MAIN/data/local/MealEntity.kt" \
        "$MAIN/data/local/ContactDao.kt" \
        "$MAIN/data/local/ContactEntity.kt" \
        "$MAIN/data/repository/MealRepository.kt" \
        "$MAIN/data/repository/MealRepositoryImpl.kt" \
        "$MAIN/data/repository/MealMappers.kt" \
        "$MAIN/data/repository/ContactRepository.kt" \
        "$MAIN/data/repository/ContactRepositoryImpl.kt" \
        "$MAIN/data/repository/ContactMappers.kt" \
        "$MAIN/di/DatabaseModule.kt" \
        "$MAIN/di/NetworkModule.kt" \
        "$MAIN/model/Meal.kt" \
        "$MAIN/model/MealCategory.kt" \
        "$MAIN/model/Contact.kt" \
        "$MAIN/ui/components/SharedTransitionModifiers.kt" \
        "app/schemas" \
        "app/src/test/java/$NEW_PATH/ui/feature/meallist" \
        "app/src/test/java/$NEW_PATH/ui/feature/mealdetail" \
        "app/src/test/java/$NEW_PATH/ui/feature/contactlist" \
        "app/src/test/java/$NEW_PATH/ui/feature/contactform" \
        "app/src/androidTest/java/$NEW_PATH/data" \
        "app/src/androidTest/java/$NEW_PATH/ui"

    cat > "$MAIN/ui/navigation/Destinations.kt" <<EOF
package $NEW_PACKAGE.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object Settings : Destinations
}
EOF

    cat > "$MAIN/ui/navigation/TopLevelDestination.kt" <<EOF
package $NEW_PACKAGE.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import $NEW_PACKAGE.R

/**
 * The tabs shown in the bottom navigation bar, in display order. Each one
 * maps to a top-level (start) destination of the nav graph.
 */
enum class TopLevelDestination(
    val route: Destinations,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    Settings(Destinations.Settings, R.string.nav_settings, Icons.Filled.Settings),
}

/** Matches Material3's standard bottom bar height; top-level screens reserve this much space. */
val BottomNavigationBarHeight: Dp = 80.dp
EOF

    cat > "$MAIN/ui/navigation/${APP_NAME}NavHost.kt" <<EOF
package $NEW_PACKAGE.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import $NEW_PACKAGE.ui.feature.settings.SettingsScreen

/**
 * The bottom nav bar is overlaid on top of [NavHost] rather than resizing around it, so [NavHost]
 * always fills the screen and the bar can fade in/out without perturbing a screen transition.
 */
@Composable
fun ${APP_NAME}NavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isTopLevelDestination =
        TopLevelDestination.entries.any { topLevel ->
            currentDestination?.hierarchy?.any { it.hasRoute(topLevel.route::class) } == true
        }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destinations.Settings,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<Destinations.Settings> {
                SettingsScreen(modifier = Modifier.padding(bottom = BottomNavigationBarHeight))
            }
        }

        ${APP_NAME}BottomBar(
            visible = isTopLevelDestination,
            currentDestination = currentDestination,
            onTabClick = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ${APP_NAME}BottomBar(
    visible: Boolean,
    currentDestination: NavDestination?,
    onTabClick: (Destinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        NavigationBar {
            TopLevelDestination.entries.forEach { topLevel ->
                val selected =
                    currentDestination?.hierarchy?.any { it.hasRoute(topLevel.route::class) } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabClick(topLevel.route) },
                    icon = { Icon(topLevel.icon, contentDescription = stringResource(topLevel.label)) },
                    label = { Text(stringResource(topLevel.label)) },
                )
            }
        }
    }
}
EOF

    cat > "$MAIN/di/RepositoryModule.kt" <<EOF
package $NEW_PACKAGE.di

import $NEW_PACKAGE.data.repository.UserPreferencesRepository
import $NEW_PACKAGE.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
EOF

    cat > "$MAIN/${APP_NAME}App.kt" <<EOF
package $NEW_PACKAGE

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ${APP_NAME}App :
    Application(),
    SingletonImageLoader.Factory {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
            .build()
}
EOF

    # Retrofit stays wired for whatever API the new app adds — only the sample's own MealApi
    # provider goes, along with its base URL.
    cat > "$MAIN/di/NetworkModule.kt" <<EOF
package $NEW_PACKAGE.di

import $NEW_PACKAGE.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val NETWORK_TIMEOUT = 30.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(NETWORK_TIMEOUT.toJavaDuration())
            .readTimeout(NETWORK_TIMEOUT.toJavaDuration())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }.build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
EOF

    cat > app/src/main/res/values/strings.xml <<EOF
<resources>
    <string name="app_name">$APP_NAME</string>

    <string name="error_network">No connection. Check your network and try again.</string>
    <string name="error_server">The server is having trouble. Try again shortly.</string>
    <string name="error_unknown">Something went wrong. Try again.</string>

    <string name="action_retry">Retry</string>

    <string name="nav_settings">Settings</string>

    <string name="settings_theme_title">Theme</string>
    <string name="settings_theme_system">System</string>
    <string name="settings_theme_light">Light</string>
    <string name="settings_theme_dark">Dark</string>
</resources>
EOF

    # The deep link, Room and Retrofit all belong to the sample features. Single-quoted perl:
    # nothing here is interpolated by the shell.
    perl -0pi -e 's{\n[^\n]*<!--[^\n]*://meal[^\n]*-->\n\s*<intent-filter>.*?</intent-filter>\n}{\n}s' \
        app/src/main/AndroidManifest.xml
    perl -0pi -e 's!// Schemas are committed.*?\n\}\n\n!!s' app/build.gradle.kts
    perl -ni -e 'print unless m!alias\(libs\.plugins\.androidx\.room\)!' app/build.gradle.kts
    perl -ni -e 'print unless m!libs\.androidx\.room!' app/build.gradle.kts
    perl -pi -e 's!MEAL_API_BASE_URL!API_BASE_URL!' app/build.gradle.kts
    perl -pi -e 's!https://www\.themealdb\.com/api/json/v1/1/!https://example.com/!' app/build.gradle.kts
fi

# ----------------------------------------------------------------------- build
echo
echo "Rewrite complete. Verifying..."
if [[ -z "${ANDROID_HOME:-}" && ! -f local.properties ]]; then
    sdk_dir="$HOME/Library/Android/sdk"
    if [[ -d "$sdk_dir" ]]; then
        export ANDROID_HOME="$sdk_dir"
    else
        echo "No Android SDK found: set ANDROID_HOME or write sdk.dir into local.properties." >&2
        exit 1
    fi
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
    studio_jbr="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    if [[ -d "$studio_jbr" ]]; then
        export JAVA_HOME="$studio_jbr"
    else
        echo "JAVA_HOME is unset and Android Studio's JBR wasn't found; set it and re-run the build." >&2
        exit 1
    fi
fi
./gradlew ktlintFormat ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug

cat <<DONE

Done. $APP_NAME is ready in $REPO_ROOT.

Next:
  - review 'git diff' / 'git status', then make your own first commit
  - CLAUDE.md still describes the scaffold's sample features; trim it to match
DONE
