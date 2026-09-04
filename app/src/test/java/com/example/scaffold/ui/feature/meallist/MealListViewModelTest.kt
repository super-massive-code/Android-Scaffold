package com.example.scaffold.ui.feature.meallist

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import com.example.scaffold.ui.components.UiError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private class FakeMealRepository(
    initialMeals: List<Meal> = emptyList(),
    private val mealsAfterRefresh: List<Meal> = emptyList(),
    private val refreshError: Throwable? = null,
    private val mealsByCategory: Map<MealCategory, List<Meal>> = emptyMap(),
    private val refreshDelayByCategory: Map<MealCategory, Long> = emptyMap(),
) : MealRepository {
    private val mealsFlow = MutableStateFlow(initialMeals)

    override fun observeMeals(): Flow<List<Meal>> = mealsFlow.asStateFlow()

    override fun observeMeal(id: String): Flow<Meal?> = mealsFlow.map { list -> list.find { it.id == id } }

    override suspend fun refresh(id: String) = Unit

    override suspend fun refreshByCategory(category: MealCategory) {
        refreshError?.let { throw it }
        delay(refreshDelayByCategory[category] ?: 0L)
        mealsFlow.value = mealsByCategory[category] ?: mealsAfterRefresh
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MealListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits content once refresh populates the cache`() =
        runTest {
            val meal = Meal(id = "1", title = "Cake", thumbnailUrl = "https://example.com/cake.jpg")
            val viewModel = MealListViewModel(FakeMealRepository(mealsAfterRefresh = listOf(meal)))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Content)
            assertEquals(listOf(meal), (state as MealListUiState.Content).meals)
        }

    @Test
    fun `emits empty content when a refresh succeeds with no meals`() =
        runTest {
            val viewModel = MealListViewModel(FakeMealRepository(mealsAfterRefresh = emptyList()))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Content)
            assertEquals(emptyList<Meal>(), (state as MealListUiState.Content).meals)
        }

    @Test
    fun `emits error when refresh fails and the cache is empty`() =
        runTest {
            val viewModel = MealListViewModel(FakeMealRepository(refreshError = IllegalStateException("boom")))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Error)
            assertEquals(UiError.Unknown, (state as MealListUiState.Error).error)
        }

    @Test
    fun `re-selecting a category cancels the refresh already in flight`() =
        runTest {
            val chicken = Meal(id = "1", title = "Chicken pie", thumbnailUrl = "https://example.com/1.jpg")
            val beef = Meal(id = "2", title = "Beef pie", thumbnailUrl = "https://example.com/2.jpg")
            // Chicken (selected on construction) resolves last, so if its refresh survived the
            // switch to Beef it would be the list the user ends up looking at.
            val viewModel =
                MealListViewModel(
                    FakeMealRepository(
                        mealsByCategory =
                            mapOf(MealCategory.Chicken to listOf(chicken), MealCategory.Beef to listOf(beef)),
                        refreshDelayByCategory =
                            mapOf(MealCategory.Chicken to 200L, MealCategory.Beef to 50L),
                    ),
                )

            viewModel.selectCategory(MealCategory.Beef)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Content)
            assertEquals(listOf(beef), (state as MealListUiState.Content).meals)
            assertEquals(MealCategory.Beef, state.selectedCategory)
        }

    @Test
    fun `a failed refresh over cached meals becomes a transient error, not an error screen`() =
        runTest {
            val meal = Meal(id = "1", title = "Cake", thumbnailUrl = "https://example.com/cake.jpg")
            val viewModel =
                MealListViewModel(
                    FakeMealRepository(initialMeals = listOf(meal), refreshError = IOException("offline")),
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Content)
            assertEquals(listOf(meal), (state as MealListUiState.Content).meals)
            assertEquals(UiError.Network, state.transientError)

            viewModel.dismissTransientError()

            assertNull((viewModel.uiState.value as MealListUiState.Content).transientError)
        }
}
