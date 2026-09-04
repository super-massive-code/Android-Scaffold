package com.example.scaffold.model

import androidx.annotation.StringRes
import com.example.scaffold.R

/** Which colour scheme the app should use, as chosen on the Settings tab. */
enum class ThemeMode(
    @param:StringRes val label: Int,
) {
    System(R.string.settings_theme_system),
    Light(R.string.settings_theme_light),
    Dark(R.string.settings_theme_dark),
}
