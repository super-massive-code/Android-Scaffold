package com.example.scaffold.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY title ASC")
    fun observeMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE id = :id")
    fun observeMeal(id: String): Flow<MealEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(meals: List<MealEntity>)

    @Query("DELETE FROM meals")
    suspend fun deleteAll()
}
