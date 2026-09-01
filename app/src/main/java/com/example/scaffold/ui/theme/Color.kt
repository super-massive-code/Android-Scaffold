// Hex literals are the definition itself for these named color constants —
// there's no further "well-named constant" to extract them into.
@file:Suppress("MagicNumber")

package com.example.scaffold.ui.theme

import androidx.compose.ui.graphics.Color

// Primary: deep teal-green — fresh/natural, fits a recipe app without leaning on cliché food red/orange.
val LightPrimary = Color(0xFF2E6F55)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFB4F1D2)
val LightOnPrimaryContainer = Color(0xFF00210F)

val DarkPrimary = Color(0xFF8FD9B4)
val DarkOnPrimary = Color(0xFF003920)
val DarkPrimaryContainer = Color(0xFF0E5138)
val DarkOnPrimaryContainer = Color(0xFFB4F1D2)

// Secondary: warm terracotta — the appetite-appealing accent, used sparingly against the green.
val LightSecondary = Color(0xFF8A5A34)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDCBD)
val LightOnSecondaryContainer = Color(0xFF2E1500)

val DarkSecondary = Color(0xFFFFB877)
val DarkOnSecondary = Color(0xFF472A00)
val DarkSecondaryContainer = Color(0xFF64411C)
val DarkOnSecondaryContainer = Color(0xFFFFDCBD)

// Tertiary: soft slate blue — a neutral third accent for anything that shouldn't compete with primary/secondary.
val LightTertiary = Color(0xFF3E5F72)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFC3E8FF)
val LightOnTertiaryContainer = Color(0xFF001E2C)

val DarkTertiary = Color(0xFFA7CDE3)
val DarkOnTertiary = Color(0xFF063142)
val DarkTertiaryContainer = Color(0xFF244859)
val DarkOnTertiaryContainer = Color(0xFFC3E8FF)

// Neutrals: warm off-white/near-black rather than stark white/grey. Every surface* and inverse*
// role is set explicitly here — leaving any of them unset falls back to Compose's own
// lightColorScheme()/darkColorScheme() baseline neutrals, which carry a purple tint left over
// from the default Material template and show up in places like the NavigationBar container.
val LightBackground = Color(0xFFFAFAF6)
val LightOnBackground = Color(0xFF1A1C19)
val LightSurfaceVariant = Color(0xFFDEE5DA)
val LightOnSurfaceVariant = Color(0xFF414942)
val LightOutline = Color(0xFF71796F)
val LightOutlineVariant = Color(0xFFC2C9BE)
val LightSurfaceDim = Color(0xFFDAD9D4)
val LightSurfaceBright = Color(0xFFFAFAF6)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF4F3EE)
val LightSurfaceContainer = Color(0xFFEEEDE8)
val LightSurfaceContainerHigh = Color(0xFFE8E7E2)
val LightSurfaceContainerHighest = Color(0xFFE2E1DC)
val LightInverseSurface = Color(0xFF2E312D)
val LightInverseOnSurface = Color(0xFFF1F2EA)

val DarkBackground = Color(0xFF12140F)
val DarkOnBackground = Color(0xFFE2E3DC)
val DarkSurfaceVariant = Color(0xFF414942)
val DarkOnSurfaceVariant = Color(0xFFC1C9BE)
val DarkOutline = Color(0xFF8B9389)
val DarkOutlineVariant = Color(0xFF414942)
val DarkSurfaceDim = Color(0xFF12140F)
val DarkSurfaceBright = Color(0xFF383A35)
val DarkSurfaceContainerLowest = Color(0xFF0C0F0A)
val DarkSurfaceContainerLow = Color(0xFF1A1C18)
val DarkSurfaceContainer = Color(0xFF1E201C)
val DarkSurfaceContainerHigh = Color(0xFF282B26)
val DarkSurfaceContainerHighest = Color(0xFF333630)
val DarkInverseSurface = Color(0xFFE2E3DC)
val DarkInverseOnSurface = Color(0xFF2E312D)

val Scrim = Color(0xFF000000)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
