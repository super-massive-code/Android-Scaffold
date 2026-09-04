package com.example.scaffold.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.UserPreferencesRepository
import com.example.scaffold.model.ThemeMode
import com.example.scaffold.ui.components.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            userPreferencesRepository
                .observeThemeMode()
                .map<ThemeMode, SettingsUiState> { themeMode -> SettingsUiState.Content(themeMode) }
                .catch { throwable -> emit(SettingsUiState.Error(throwable.toUiError())) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = SettingsUiState.Loading,
                )

        fun selectThemeMode(themeMode: ThemeMode) {
            viewModelScope.launch {
                userPreferencesRepository.setThemeMode(themeMode)
            }
        }
    }
