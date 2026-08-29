package com.example.scaffold.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
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
