package com.example.scaffold.ui.feature.meallist

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import com.example.scaffold.ui.components.UiError
import com.example.scaffold.ui.theme.ScaffoldTheme

private val PreviewMeals =
    listOf(
        Meal(id = "52772", title = "Teriyaki Chicken Casserole", thumbnailUrl = ""),
        Meal(id = "52940", title = "Brown Stew Chicken", thumbnailUrl = ""),
        Meal(id = "53049", title = "Apam balik", thumbnailUrl = ""),
    )

@PreviewLightDark
@Composable
private fun MealListScreenContentPreview() {
    ScaffoldTheme {
        MealListScreen(
            uiState = MealListUiState.Content(meals = PreviewMeals, selectedCategory = MealCategory.Chicken),
            onMealClick = {},
            onCategorySelected = {},
            onRefresh = {},
            onTransientErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MealListScreenEmptyPreview() {
    ScaffoldTheme {
        MealListScreen(
            uiState = MealListUiState.Content(meals = emptyList(), selectedCategory = MealCategory.Seafood),
            onMealClick = {},
            onCategorySelected = {},
            onRefresh = {},
            onTransientErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MealListScreenLoadingPreview() {
    ScaffoldTheme {
        MealListScreen(
            uiState = MealListUiState.Loading,
            onMealClick = {},
            onCategorySelected = {},
            onRefresh = {},
            onTransientErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MealListScreenErrorPreview() {
    ScaffoldTheme {
        MealListScreen(
            uiState = MealListUiState.Error(UiError.Network),
            onMealClick = {},
            onCategorySelected = {},
            onRefresh = {},
            onTransientErrorShown = {},
        )
    }
}
