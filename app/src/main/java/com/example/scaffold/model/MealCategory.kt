package com.example.scaffold.model

import androidx.annotation.StringRes
import com.example.scaffold.R

enum class MealCategory(
    val apiValue: String,
    @param:StringRes val label: Int,
) {
    Chicken("Chicken", R.string.meal_cat_chicken),
    Beef("Beef", R.string.meal_cat_beef),
    Dessert("Dessert", R.string.meal_cat_dessert),
    Seafood("Seafood", R.string.meal_cat_seafood),
    Vegetarian("Vegetarian", R.string.meal_cat_vegetarian),
}
