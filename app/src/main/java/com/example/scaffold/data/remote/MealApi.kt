package com.example.scaffold.data.remote

import com.example.scaffold.data.remote.dto.MealDetailResponse
import com.example.scaffold.data.remote.dto.MealSummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {
    @GET("filter.php")
    suspend fun getMealsByCategory(
        @Query("c") category: String,
    ): MealSummaryResponse

    @GET("lookup.php")
    suspend fun getMealDetail(
        @Query("i") id: String,
    ): MealDetailResponse
}
