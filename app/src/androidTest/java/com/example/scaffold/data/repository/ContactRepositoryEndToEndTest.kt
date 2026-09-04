package com.example.scaffold.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scaffold.data.local.AppDatabase
import com.example.scaffold.model.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ContactRepositoryImpl] against a real in-memory Room database — the local-only counterpart to
 * [MealRepositoryEndToEndTest]. A fake `ContactDao` can't catch what this covers: whether the
 * mapped entity actually carries the primary key `@Update`/`@Delete` match on.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ContactRepositoryEndToEndTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ContactRepository

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    AppDatabase::class.java,
                ).build()
        repository = ContactRepositoryImpl(database.contactDao(), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveContact_assignsAnIdAndTheContactIsObservable() =
        runTest {
            repository.saveContact(ada())

            val saved = repository.observeContacts().first().single()
            assertEquals("Ada", saved.firstName)
            assertEquals(saved, repository.observeContact(saved.id).first())
        }

    @Test
    fun updateContact_writesOverTheExistingRow() =
        runTest {
            repository.saveContact(ada())
            val saved = repository.observeContacts().first().single()

            repository.updateContact(saved.copy(city = "Southsea"))

            val contacts = repository.observeContacts().first()
            assertEquals(1, contacts.size)
            assertEquals("Southsea", contacts.single().city)
            assertEquals(saved.id, contacts.single().id)
        }

    @Test
    fun deleteContact_removesTheRowAndUndoRestoresItWithTheSameId() =
        runTest {
            repository.saveContact(ada())
            val saved = repository.observeContacts().first().single()

            repository.deleteContact(saved)

            assertEquals(emptyList<Contact>(), repository.observeContacts().first())
            assertNull(repository.observeContact(saved.id).first())

            // What the contact list's undo action does: re-insert the contact it deleted.
            repository.saveContact(saved)

            assertEquals(saved, repository.observeContacts().first().single())
        }

    @Test
    fun seedIfEmpty_onlySeedsAnEmptyStore() =
        runTest {
            repository.seedIfEmpty()
            val seeded = repository.observeContacts().first()

            repository.seedIfEmpty()

            assertEquals(seeded, repository.observeContacts().first())
        }

    private fun ada() =
        Contact(
            firstName = "Ada",
            lastName = "Lovelace",
            addressLine1 = "12 Curzon Street",
            addressLine2 = null,
            city = "London",
            postcode = "W1J 5HN",
        )
}
