package com.example.scaffold.data.repository

import com.example.scaffold.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun observeContacts(): Flow<List<Contact>>

    suspend fun saveContact(contact: Contact)

    suspend fun seedIfEmpty()
}
