package com.example.scaffold.data.repository

import com.example.scaffold.data.local.ContactDao
import com.example.scaffold.di.IoDispatcher
import com.example.scaffold.model.Contact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactRepositoryImpl
    @Inject
    constructor(
        private val contactDao: ContactDao,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ContactRepository {
        override fun observeContacts(): Flow<List<Contact>> =
            contactDao.observeContacts().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun saveContact(contact: Contact) {
            withContext(ioDispatcher) {
                contactDao.insert(contact.toEntity())
            }
        }

        override suspend fun seedIfEmpty() =
            withContext(ioDispatcher) {
                if (contactDao.observeContacts().first().isEmpty()) {
                    DummyContacts.forEach { contactDao.insert(it.toEntity()) }
                }
            }
    }

// Lets a fresh install show the Contacts tab with something in it rather than the empty state.
private val DummyContacts =
    listOf(
        Contact(
            firstName = "Ada",
            lastName = "Lovelace",
            addressLine1 = "12 Curzon Street",
            addressLine2 = null,
            city = "London",
            postcode = "W1J 5HN",
        ),
        Contact(
            firstName = "Alan",
            lastName = "Turing",
            addressLine1 = "Hut 8, Bletchley Park",
            addressLine2 = "Sherwood Drive",
            city = "Milton Keynes",
            postcode = "MK3 6EB",
        ),
        Contact(
            firstName = "Grace",
            lastName = "Hopper",
            addressLine1 = "45 Harbour Road",
            addressLine2 = null,
            city = "Portsmouth",
            postcode = "PO1 3AX",
        ),
    )
