package com.example.scaffold.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String,
    val instructions: String? = null,
)
