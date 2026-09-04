package com.example.scaffold.ui.feature.meallist

import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import com.example.scaffold.ui.components.UiError

sealed interface MealListUiState {
    data object Loading : MealListUiState

    data class Error(
        val error: UiError,
    ) : MealListUiState

    /**
     * [transientError] is a refresh failure that happened while there was already something
     * to look at — shown as a snackbar over the list, not in place of it. [isRefreshing] drives
     * the pull-to-refresh indicator; a refresh with nothing cached yet is [Loading] instead.
     */
    data class Content(
        val meals: List<Meal>,
        val selectedCategory: MealCategory,
        val transientError: UiError? = null,
        val isRefreshing: Boolean = false,
    ) : MealListUiState
}
