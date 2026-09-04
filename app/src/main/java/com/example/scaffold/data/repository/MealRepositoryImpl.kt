package com.example.scaffold.data.repository

import com.example.scaffold.data.local.MealDao
import com.example.scaffold.data.remote.MealApi
import com.example.scaffold.di.IoDispatcher
import com.example.scaffold.model.Meal
import com.example.scaffold.model.MealCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MealRepositoryImpl
    @Inject
    constructor(
        private val mealApi: MealApi,
        private val mealDao: MealDao,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : MealRepository {
        override fun observeMeals(): Flow<List<Meal>> =
            mealDao.observeMeals().map { entities -> entities.map { it.toDomain() } }

        override fun observeMeal(id: String): Flow<Meal?> = mealDao.observeMeal(id).map { it?.toDomain() }

        override suspend fun refresh(id: String) =
            withContext(ioDispatcher) {
                // A recipe's instructions don't change once published, so once we have them
                // cached there's no need to hit the network again every time the detail
                // screen reopens.
                if (mealDao.observeMeal(id).first()?.instructions != null) return@withContext
                val meal =
                    mealApi
                        .getMealDetail(id)
                        .meals
                        .orEmpty()
                        .firstOrNull() ?: return@withContext
                mealDao.upsertAll(listOf(meal.toEntity()))
            }

        override suspend fun refreshByCategory(category: MealCategory) =
            withContext(ioDispatcher) {
                // Fetch before touching the cache: a failed request must leave the previously
                // cached meals in place, or offline-first reads degrade to an empty screen.
                val meals = mealApi.getMealsByCategory(category.apiValue).meals.orEmpty()
                mealDao.replaceAll(meals.map { it.toEntity() })
            }
    }
