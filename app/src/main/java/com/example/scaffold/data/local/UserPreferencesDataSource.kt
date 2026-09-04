package com.example.scaffold.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.scaffold.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val ThemeModeKey = stringPreferencesKey("theme_mode")

/**
 * The DataStore counterpart to a Room `@Dao`: the lowest layer that knows about storage keys and
 * their encoding, exposing a `Flow` of domain values upwards. A repository sits on top of it, the
 * same way `MealRepositoryImpl` sits on `MealDao`.
 */
class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        fun observeThemeMode(): Flow<ThemeMode> =
            dataStore.data.map { preferences ->
                // An unknown or missing stored value means "never set, or written by a version
                // that had a mode this one doesn't" — either way, follow the system.
                preferences[ThemeModeKey]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.System
            }

        suspend fun setThemeMode(themeMode: ThemeMode) {
            dataStore.edit { preferences -> preferences[ThemeModeKey] = themeMode.name }
        }
    }
