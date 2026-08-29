package com.example.scaffold.ui.feature.meallist

import com.example.scaffold.model.Meal

sealed interface MealListUiState {
    data object Loading : MealListUiState

    data class Error(
        val message: String?,
    ) : MealListUiState

    data class Content(
        val meals: List<Meal>,
    ) : MealListUiState
}
