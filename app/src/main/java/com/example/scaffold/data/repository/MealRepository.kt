package com.example.scaffold.data.repository

import com.example.scaffold.model.Meal
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeMeals(): Flow<List<Meal>>

    fun observeMeal(id: String): Flow<Meal?>

    suspend fun refresh()

    suspend fun refresh(id: String)
}
