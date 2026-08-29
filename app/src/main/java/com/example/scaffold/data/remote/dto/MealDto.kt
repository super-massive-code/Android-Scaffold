package com.example.scaffold.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealSummaryDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
)

@Serializable
data class MealSummaryResponse(
    val meals: List<MealSummaryDto>? = null,
)

@Serializable
data class MealDetailDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String,
)

@Serializable
data class MealDetailResponse(
    val meals: List<MealDetailDto>? = null,
)
