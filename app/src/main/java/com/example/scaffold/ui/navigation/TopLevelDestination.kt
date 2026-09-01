package com.example.scaffold.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.scaffold.R

/**
 * The tabs shown in the bottom navigation bar, in display order. Each one
 * maps to a top-level (start) destination of the nav graph.
 */
enum class TopLevelDestination(
    val route: Destinations,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    Contacts(Destinations.ContactList, R.string.nav_contacts, Icons.Filled.Person),
    Meals(Destinations.MealList, R.string.nav_meals, Icons.AutoMirrored.Filled.List),
}

/**
 * Matches Material3's standard [androidx.compose.material3.NavigationBar] height. [ScaffoldNavHost]
 * overlays the bottom bar on top of the nav graph rather than resizing it around the bar, so its
 * appearance/disappearance never changes the content area a screen transition animates within —
 * top-level screens reserve this much space themselves instead, as a fixed inset that never
 * changes size, so it can't fight an in-flight transition (e.g. the meal list/detail shared
 * element) the way a dynamically-sized bottom bar did.
 */
val BottomNavigationBarHeight: Dp = 80.dp
