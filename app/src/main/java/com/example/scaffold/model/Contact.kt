package com.example.scaffold.model

data class Contact(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val postcode: String,
)
