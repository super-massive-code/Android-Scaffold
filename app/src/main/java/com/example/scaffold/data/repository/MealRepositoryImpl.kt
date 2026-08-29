package com.example.scaffold.data.repository

import com.example.scaffold.data.local.MealDao
import com.example.scaffold.data.remote.MealApi
import com.example.scaffold.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val MEAL_CATEGORY = "Chicken"

class MealRepositoryImpl
    @Inject
    constructor(
        private val mealApi: MealApi,
        private val mealDao: MealDao,
    ) : MealRepository {
        override fun observeMeals(): Flow<List<Meal>> =
            mealDao.observeMeals().map { entities -> entities.map { it.toDomain() } }

        override fun observeMeal(id: String): Flow<Meal?> = mealDao.observeMeal(id).map { it?.toDomain() }

        override suspend fun refresh() {
            val meals = mealApi.getMealsByCategory(MEAL_CATEGORY).meals.orEmpty()
            mealDao.upsertAll(meals.map { it.toEntity() })
        }

        override suspend fun refresh(id: String) {
            val meal =
                mealApi
                    .getMealDetail(id)
                    .meals
                    .orEmpty()
                    .firstOrNull() ?: return
            mealDao.upsertAll(listOf(meal.toEntity()))
        }
    }
