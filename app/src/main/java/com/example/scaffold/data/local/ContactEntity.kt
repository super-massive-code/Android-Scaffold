package com.example.scaffold.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val postcode: String,
)
