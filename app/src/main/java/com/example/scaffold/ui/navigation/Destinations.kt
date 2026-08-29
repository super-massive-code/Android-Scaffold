package com.example.scaffold.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object ContactList : Destinations

    @Serializable
    data object MealList : Destinations

    @Serializable
    data class MealDetail(
        val mealId: String,
    ) : Destinations

    @Serializable
    data object ContactForm : Destinations
}
