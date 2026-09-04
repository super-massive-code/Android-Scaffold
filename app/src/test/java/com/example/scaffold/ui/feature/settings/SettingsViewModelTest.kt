package com.example.scaffold.ui.feature.settings

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.UserPreferencesRepository
import com.example.scaffold.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeUserPreferencesRepository(
    initialThemeMode: ThemeMode = ThemeMode.System,
) : UserPreferencesRepository {
    private val themeMode = MutableStateFlow(initialThemeMode)

    override fun observeThemeMode(): Flow<ThemeMode> = themeMode.asStateFlow()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        this.themeMode.value = themeMode
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `reflects the stored theme mode as content`() =
        runTest {
            val viewModel = SettingsViewModel(FakeUserPreferencesRepository(ThemeMode.Dark))
            backgroundScope.launch { viewModel.uiState.collect {} }

            advanceUntilIdle()

            assertEquals(SettingsUiState.Content(ThemeMode.Dark), viewModel.uiState.value)
        }

    @Test
    fun `selecting a theme mode writes it and the new value comes back`() =
        runTest {
            val viewModel = SettingsViewModel(FakeUserPreferencesRepository(ThemeMode.System))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.selectThemeMode(ThemeMode.Light)
            advanceUntilIdle()

            assertEquals(SettingsUiState.Content(ThemeMode.Light), viewModel.uiState.value)
        }
}
