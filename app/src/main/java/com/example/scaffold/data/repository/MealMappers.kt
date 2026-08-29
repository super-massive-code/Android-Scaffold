package com.example.scaffold.data.repository

import com.example.scaffold.data.local.MealEntity
import com.example.scaffold.data.remote.dto.MealDetailDto
import com.example.scaffold.data.remote.dto.MealSummaryDto
import com.example.scaffold.model.Meal

fun MealSummaryDto.toEntity(): MealEntity = MealEntity(id = idMeal, title = strMeal, thumbnailUrl = strMealThumb)

fun MealDetailDto.toEntity(): MealEntity =
    MealEntity(id = idMeal, title = strMeal, thumbnailUrl = strMealThumb, instructions = strInstructions)

fun MealEntity.toDomain(): Meal = Meal(id = id, title = title, thumbnailUrl = thumbnailUrl, instructions = instructions)
