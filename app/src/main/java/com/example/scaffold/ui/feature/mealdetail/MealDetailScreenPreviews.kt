package com.example.scaffold.ui.feature.mealdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.scaffold.model.Meal
import com.example.scaffold.ui.components.UiError
import com.example.scaffold.ui.theme.ScaffoldTheme

private val PreviewMeal =
    Meal(
        id = "52772",
        title = "Teriyaki Chicken Casserole",
        thumbnailUrl = "",
        instructions =
            "Preheat oven to 350F. Spray a 9x13-inch baking pan with non-stick spray. " +
                "Combine soy sauce, water, brown sugar, ginger and garlic in a saucepan and " +
                "cover over medium heat until it starts to simmer.",
    )

@PreviewLightDark
@Composable
private fun MealDetailScreenContentPreview() {
    ScaffoldTheme {
        MealDetailScreen(
            uiState = MealDetailUiState.Content(meal = PreviewMeal),
            onBack = {},
            onRetry = {},
            onTransientErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MealDetailScreenLoadingPreview() {
    ScaffoldTheme {
        MealDetailScreen(
            uiState = MealDetailUiState.Loading,
            onBack = {},
            onRetry = {},
            onTransientErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MealDetailScreenErrorPreview() {
    ScaffoldTheme {
        MealDetailScreen(
            uiState = MealDetailUiState.Error(UiError.Server),
            onBack = {},
            onRetry = {},
            onTransientErrorShown = {},
        )
    }
}
