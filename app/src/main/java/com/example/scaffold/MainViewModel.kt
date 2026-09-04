package com.example.scaffold

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.UserPreferencesRepository
import com.example.scaffold.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * The app shell's own ViewModel: [MainActivity] isn't a feature screen, but it still needs one
 * piece of state — the theme to render everything else in — and reading it here keeps the
 * repository out of the Activity, same as on every feature screen.
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val themeMode: StateFlow<ThemeMode> =
            userPreferencesRepository
                .observeThemeMode()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = ThemeMode.System,
                )
    }
