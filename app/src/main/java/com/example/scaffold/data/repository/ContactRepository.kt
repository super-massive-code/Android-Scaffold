package com.example.scaffold.data.repository

import com.example.scaffold.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun observeContacts(): Flow<List<Contact>>

    fun observeContact(id: Long): Flow<Contact?>

    suspend fun saveContact(contact: Contact)

    suspend fun updateContact(contact: Contact)

    suspend fun deleteContact(contact: Contact)

    suspend fun seedIfEmpty()
}
