package com.example.scaffold.data.repository

import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeMeals(): Flow<List<Meal>>

    fun observeMeal(id: String): Flow<Meal?>

    suspend fun refresh(id: String)

    suspend fun refreshByCategory(category: MealCategory)
}
