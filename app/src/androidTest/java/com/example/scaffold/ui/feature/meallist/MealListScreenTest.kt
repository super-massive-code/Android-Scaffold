package com.example.scaffold.ui.feature.meallist

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the stateless [MealListScreen] overload directly: no ViewModel, no fake repository, no
 * Hilt — a UiState in, callbacks out, which is all the Composable is. What the ViewModel does
 * with those callbacks is [MealListViewModelTest]'s job.
 */
@RunWith(AndroidJUnit4::class)
class MealListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingAMeal_invokesOnMealClickWithItsId() {
        var clickedId: String? = null

        composeTestRule.setContent {
            MealListScreen(
                uiState = contentWith(Meal(id = "7", title = "Trifle", thumbnailUrl = "")),
                onMealClick = { clickedId = it },
                onCategorySelected = {},
                onRetry = {},
                onTransientErrorShown = {},
            )
        }

        composeTestRule.onNodeWithText("Trifle").performClick()

        assertEquals("7", clickedId)
    }

    @Test
    fun tappingACategoryChip_invokesOnCategorySelectedWithIt() {
        var selected: MealCategory? = null

        composeTestRule.setContent {
            MealListScreen(
                uiState = contentWith(Meal(id = "7", title = "Trifle", thumbnailUrl = "")),
                onMealClick = {},
                onCategorySelected = { selected = it },
                onRetry = {},
                onTransientErrorShown = {},
            )
        }

        composeTestRule.onNodeWithText("Dessert").performClick()

        assertEquals(MealCategory.Dessert, selected)
    }
}

private fun contentWith(vararg meals: Meal) =
    MealListUiState.Content(meals = meals.toList(), selectedCategory = MealCategory.Chicken)
