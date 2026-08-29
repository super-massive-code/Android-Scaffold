package com.example.scaffold.ui.feature.mealdetail

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private class FakeMealRepository(
    private val mealAfterRefresh: Meal? = null,
    private val refreshError: Throwable? = null,
) : MealRepository {
    private val mealsFlow = MutableStateFlow<List<Meal>>(emptyList())

    override fun observeMeals(): Flow<List<Meal>> = mealsFlow.asStateFlow()

    override fun observeMeal(id: String): Flow<Meal?> = mealsFlow.map { list -> list.find { it.id == id } }

    override suspend fun refresh() = Unit

    override suspend fun refresh(id: String) {
        refreshError?.let { throw it }
        mealAfterRefresh?.let { mealsFlow.value = listOf(it) }
    }
}

/**
 * Runs as an instrumented test, not a plain JVM unit test, because [MealDetailViewModel]'s
 * `SavedStateHandle.toRoute<Destinations.MealDetail>()` call goes through
 * `androidx.core.os.BundleKt.bundleOf`, and `android.os.Bundle` is stubbed to throw
 * ("not mocked") outside a real Android runtime — there's no `MainDispatcherRule`-only way
 * to unit test a ViewModel that reads a type-safe nav argument.
 *
 * `Dispatchers.Main` still needs pointing at an unconfined test dispatcher here, same as
 * `MainDispatcherRule` does for the JVM tests — on a real device `Dispatchers.Main` is the
 * actual main-looper dispatcher, and `viewModelScope`'s launches would otherwise race the
 * test body instead of running eagerly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MealDetailViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emitsContentOnceRefreshPopulatesTheInstructions() =
        runTest {
            val meal =
                Meal(
                    id = "1",
                    title = "Cake",
                    thumbnailUrl = "https://example.com/cake.jpg",
                    instructions = "Bake it.",
                )
            val viewModel =
                MealDetailViewModel(
                    SavedStateHandle(mapOf("mealId" to "1")),
                    FakeMealRepository(mealAfterRefresh = meal),
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealDetailUiState.Content)
            assertEquals(meal, (state as MealDetailUiState.Content).meal)
        }

    @Test
    fun emitsErrorWhenRefreshFailsAndNothingIsCachedYet() =
        runTest {
            val viewModel =
                MealDetailViewModel(
                    SavedStateHandle(mapOf("mealId" to "1")),
                    FakeMealRepository(refreshError = IllegalStateException("boom")),
                )

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is MealDetailUiState.Error)
            assertEquals("boom", (state as MealDetailUiState.Error).message)
        }
}
