package com.example.scaffold.data.repository

import com.example.scaffold.data.local.MealDao
import com.example.scaffold.data.local.MealEntity
import com.example.scaffold.data.remote.MealApi
import com.example.scaffold.data.remote.dto.MealDetailDto
import com.example.scaffold.data.remote.dto.MealDetailResponse
import com.example.scaffold.data.remote.dto.MealSummaryDto
import com.example.scaffold.data.remote.dto.MealSummaryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeMealApi(
    private val summaries: List<MealSummaryDto> = emptyList(),
    private val details: Map<String, MealDetailDto> = emptyMap(),
) : MealApi {
    override suspend fun getMealsByCategory(category: String): MealSummaryResponse = MealSummaryResponse(summaries)

    override suspend fun getMealDetail(id: String): MealDetailResponse = MealDetailResponse(listOfNotNull(details[id]))
}

private class FakeMealDao : MealDao {
    private val table = MutableStateFlow<List<MealEntity>>(emptyList())

    override fun observeMeals(): Flow<List<MealEntity>> = table.asStateFlow()

    override fun observeMeal(id: String): Flow<MealEntity?> = table.map { list -> list.find { it.id == id } }

    override suspend fun upsertAll(meals: List<MealEntity>) {
        val untouched = table.value.filterNot { existing -> meals.any { it.id == existing.id } }
        table.value = untouched + meals
    }
}

class MealRepositoryImplTest {
    @Test
    fun `refresh fetches from the api and caches the result`() =
        runTest {
            val dto = MealSummaryDto(idMeal = "1", strMeal = "Cake", strMealThumb = "https://example.com/cake.jpg")
            val repository: MealRepository = MealRepositoryImpl(FakeMealApi(summaries = listOf(dto)), FakeMealDao())

            repository.refresh()

            val meals = repository.observeMeals().first()
            assertEquals(1, meals.size)
            assertEquals("Cake", meals.first().title)
        }

    @Test
    fun `observeMeal maps the matching cached entity to a domain model`() =
        runTest {
            val dto = MealSummaryDto(idMeal = "5", strMeal = "Pie", strMealThumb = "https://example.com/pie.jpg")
            val repository: MealRepository = MealRepositoryImpl(FakeMealApi(summaries = listOf(dto)), FakeMealDao())

            repository.refresh()

            val meal = repository.observeMeal("5").first()
            assertEquals("Pie", meal?.title)
        }

    @Test
    fun `refresh with an id fetches and caches the full meal detail`() =
        runTest {
            val detail =
                MealDetailDto(
                    idMeal = "7",
                    strMeal = "Trifle",
                    strMealThumb = "https://example.com/trifle.jpg",
                    strInstructions = "Layer it up.",
                )
            val repository: MealRepository =
                MealRepositoryImpl(FakeMealApi(details = mapOf("7" to detail)), FakeMealDao())

            repository.refresh("7")

            val meal = repository.observeMeal("7").first()
            assertEquals("Layer it up.", meal?.instructions)
        }
}
