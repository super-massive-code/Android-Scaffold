package com.example.scaffold.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * Swaps the whole cached list for [meals] in one transaction, so an observer never sees
     * the gap between the old list being dropped and the new one landing.
     */
    @Transaction
    suspend fun replaceAll(meals: List<MealEntity>) {
        deleteAll()
        upsertAll(meals)
    }
}
