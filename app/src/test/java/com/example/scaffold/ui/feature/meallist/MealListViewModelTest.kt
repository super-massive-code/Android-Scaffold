package com.example.scaffold.ui.feature.meallist

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeMealRepository(
    private val mealsAfterRefresh: List<Meal> = emptyList(),
    private val refreshError: Throwable? = null,
) : MealRepository {
    private val mealsFlow = MutableStateFlow<List<Meal>>(emptyList())

    override fun observeMeals(): Flow<List<Meal>> = mealsFlow.asStateFlow()

    override fun observeMeal(id: String): Flow<Meal?> = mealsFlow.map { list -> list.find { it.id == id } }

    override suspend fun refresh(id: String) = Unit

    override suspend fun refreshByCategory(category: MealCategory) {
        refreshError?.let { throw it }
        mealsFlow.value = mealsAfterRefresh
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
    fun `emits error when refresh fails and the cache is empty`() =
        runTest {
            val viewModel = MealListViewModel(FakeMealRepository(refreshError = IllegalStateException("boom")))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealListUiState.Error)
            assertEquals("boom", (state as MealListUiState.Error).message)
        }
}
