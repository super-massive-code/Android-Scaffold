package com.example.scaffold.ui.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.scaffold.model.ThemeMode
import com.example.scaffold.ui.components.UiError
import com.example.scaffold.ui.theme.ScaffoldTheme

@PreviewLightDark
@Composable
private fun SettingsScreenContentPreview() {
    PreviewSettings(SettingsUiState.Content(ThemeMode.System))
}

@PreviewLightDark
@Composable
private fun SettingsScreenDarkSelectedPreview() {
    PreviewSettings(SettingsUiState.Content(ThemeMode.Dark))
}

@PreviewLightDark
@Composable
private fun SettingsScreenLoadingPreview() {
    PreviewSettings(SettingsUiState.Loading)
}

@PreviewLightDark
@Composable
private fun SettingsScreenErrorPreview() {
    PreviewSettings(SettingsUiState.Error(UiError.Unknown))
}

@Composable
private fun PreviewSettings(uiState: SettingsUiState) {
    ScaffoldTheme {
        SettingsScreen(uiState = uiState, onThemeModeSelected = {})
    }
}
