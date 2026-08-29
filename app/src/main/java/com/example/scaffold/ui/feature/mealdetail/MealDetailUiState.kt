package com.example.scaffold.ui.feature.mealdetail

import com.example.scaffold.model.Meal

sealed interface MealDetailUiState {
    data object Loading : MealDetailUiState

    data class Error(
        val message: String?,
    ) : MealDetailUiState

    data class Content(
        val meal: Meal,
    ) : MealDetailUiState
}
