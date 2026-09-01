package com.example.scaffold.ui.feature.contactlist

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeContactRepository(
    initialContacts: List<Contact> = emptyList(),
) : ContactRepository {
    private val contactsFlow = MutableStateFlow(initialContacts)

    override fun observeContacts(): Flow<List<Contact>> = contactsFlow.asStateFlow()

    override suspend fun saveContact(contact: Contact) {
        contactsFlow.value = contactsFlow.value + contact
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
            val contact =
                Contact(
                    id = 1,
                    firstName = "Ada",
                    lastName = "Lovelace",
                    addressLine1 = "12 Analytical Engine Way",
                    addressLine2 = null,
                    city = "London",
                    postcode = "SW1A 1AA",
                )
            val viewModel = ContactListViewModel(FakeContactRepository(initialContacts = listOf(contact)))
            backgroundScope.launch { viewModel.uiState.collect {} }

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ContactListUiState.Content)
            assertEquals(listOf(contact), (state as ContactListUiState.Content).contacts)
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
}
