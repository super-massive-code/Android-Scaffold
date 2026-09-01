package com.example.scaffold.ui.feature.meallist

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * No Hilt test infrastructure needed here: [MealListScreen] already accepts an explicit
 * `viewModel` parameter, so a plain (non-Hilt) [MealListViewModel] built on a hand-written
 * fake repository — the same style used in [MealListViewModelTest] — is enough to drive the
 * real Composable through Compose's actual rendering and click handling.
 */
private class FakeMealRepository(
    mealsAfterRefresh: List<Meal>,
) : MealRepository {
    private val mealsFlow = MutableStateFlow(mealsAfterRefresh)

    override fun observeMeals(): Flow<List<Meal>> = mealsFlow.asStateFlow()

    override fun observeMeal(id: String): Flow<Meal?> = mealsFlow.map { list -> list.find { it.id == id } }

    override suspend fun refresh(id: String) = Unit

    override suspend fun refreshByCategory(category: MealCategory) = Unit
}

@RunWith(AndroidJUnit4::class)
class MealListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingAMeal_invokesOnMealClickWithItsId() {
        val meal = Meal(id = "7", title = "Trifle", thumbnailUrl = "https://example.com/trifle.jpg")
        var clickedId: String? = null

        composeTestRule.setContent {
            MealListScreen(
                onMealClick = { clickedId = it },
                viewModel = MealListViewModel(FakeMealRepository(listOf(meal))),
            )
        }

        composeTestRule.onNodeWithText("Trifle").performClick()

        assertEquals("7", clickedId)
    }
}
