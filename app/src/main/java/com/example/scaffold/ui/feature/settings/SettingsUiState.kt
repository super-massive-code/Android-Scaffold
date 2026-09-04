package com.example.scaffold.ui.feature.settings

import com.example.scaffold.model.ThemeMode
import com.example.scaffold.ui.components.UiError

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Error(
        val error: UiError,
    ) : SettingsUiState

    data class Content(
        val themeMode: ThemeMode,
    ) : SettingsUiState
}
