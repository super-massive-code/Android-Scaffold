package com.example.scaffold.ui.feature.mealdetail

import com.example.scaffold.model.Meal
import com.example.scaffold.ui.components.UiError

sealed interface MealDetailUiState {
    data object Loading : MealDetailUiState

    data class Error(
        val error: UiError,
    ) : MealDetailUiState

    /** See `MealListUiState.Content` for what [transientError] is for. */
    data class Content(
        val meal: Meal,
        val transientError: UiError? = null,
    ) : MealDetailUiState
}
