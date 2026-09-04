package com.example.scaffold.data.repository

import com.example.scaffold.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(themeMode: ThemeMode)
}
