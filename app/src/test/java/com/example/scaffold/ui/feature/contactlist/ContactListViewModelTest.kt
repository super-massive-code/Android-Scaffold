package com.example.scaffold.ui.feature.contactlist

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeContactRepository(
    initialContacts: List<Contact> = emptyList(),
) : ContactRepository {
    private val contactsFlow = MutableStateFlow(initialContacts)

    override fun observeContacts(): Flow<List<Contact>> = contactsFlow.asStateFlow()

    override fun observeContact(id: Long): Flow<Contact?> =
        contactsFlow.map { contacts -> contacts.find { it.id == id } }

    override suspend fun saveContact(contact: Contact) {
        contactsFlow.value = contactsFlow.value + contact
    }

    override suspend fun updateContact(contact: Contact) {
        contactsFlow.value = contactsFlow.value.map { if (it.id == contact.id) contact else it }
    }

    override suspend fun deleteContact(contact: Contact) {
        contactsFlow.value = contactsFlow.value - contact
    }

    override suspend fun seedIfEmpty() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `reflects contacts from the repository as content`() =
        runTest {
            val viewModel = ContactListViewModel(FakeContactRepository(initialContacts = listOf(Ada)))
            backgroundScope.launch { viewModel.uiState.collect {} }

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ContactListUiState.Content)
            assertEquals(listOf(Ada), (state as ContactListUiState.Content).contacts)
        }

    @Test
    fun `starts empty when the repository has no contacts`() =
        runTest {
            val viewModel = ContactListViewModel(FakeContactRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ContactListUiState.Content)
            assertTrue((state as ContactListUiState.Content).contacts.isEmpty())
        }

    @Test
    fun `deleting a contact removes it and offers an undo`() =
        runTest {
            val repository = FakeContactRepository(initialContacts = listOf(Ada))
            val viewModel = ContactListViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.deleteContact(Ada)
            advanceUntilIdle()

            val state = viewModel.uiState.value as ContactListUiState.Content
            assertTrue(state.contacts.isEmpty())
            assertEquals(Ada, state.recentlyDeleted)
        }

    @Test
    fun `undoing a delete puts the contact back with its original id`() =
        runTest {
            val repository = FakeContactRepository(initialContacts = listOf(Ada))
            val viewModel = ContactListViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.deleteContact(Ada)
            advanceUntilIdle()

            viewModel.undoDelete()
            advanceUntilIdle()

            val state = viewModel.uiState.value as ContactListUiState.Content
            assertEquals(listOf(Ada), state.contacts)
            assertNull(state.recentlyDeleted)
        }

    @Test
    fun `dismissing the undo leaves the contact deleted`() =
        runTest {
            val repository = FakeContactRepository(initialContacts = listOf(Ada))
            val viewModel = ContactListViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.deleteContact(Ada)
            advanceUntilIdle()

            viewModel.dismissUndo()
            advanceUntilIdle()

            val state = viewModel.uiState.value as ContactListUiState.Content
            assertTrue(state.contacts.isEmpty())
            assertNull(state.recentlyDeleted)
        }
}

private val Ada =
    Contact(
        id = 1,
        firstName = "Ada",
        lastName = "Lovelace",
        addressLine1 = "12 Analytical Engine Way",
        addressLine2 = null,
        city = "London",
        postcode = "SW1A 1AA",
    )
