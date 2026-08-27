package com.example.scaffold.data.repository

import com.example.scaffold.data.local.ContactDao
import com.example.scaffold.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepositoryImpl
    @Inject
    constructor(
        private val contactDao: ContactDao,
    ) : ContactRepository {
        override fun observeContacts(): Flow<List<Contact>> =
            contactDao.observeContacts().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun saveContact(contact: Contact) {
            contactDao.insert(contact.toEntity())
        }
    }
