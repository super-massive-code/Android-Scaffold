package com.example.scaffold.model

data class Meal(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val instructions: String? = null,
)
