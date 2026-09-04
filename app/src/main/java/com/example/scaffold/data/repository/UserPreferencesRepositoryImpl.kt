package com.example.scaffold.data.repository

import com.example.scaffold.data.local.UserPreferencesDataSource
import com.example.scaffold.di.IoDispatcher
import com.example.scaffold.model.ThemeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserPreferencesRepositoryImpl
    @Inject
    constructor(
        private val dataSource: UserPreferencesDataSource,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : UserPreferencesRepository {
        override fun observeThemeMode(): Flow<ThemeMode> = dataSource.observeThemeMode()

        override suspend fun setThemeMode(themeMode: ThemeMode) =
            withContext(ioDispatcher) {
                dataSource.setThemeMode(themeMode)
            }
    }
