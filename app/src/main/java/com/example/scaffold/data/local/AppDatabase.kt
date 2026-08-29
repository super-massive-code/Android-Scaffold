package com.example.scaffold.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MealEntity::class, ContactEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    abstract fun contactDao(): ContactDao
}
