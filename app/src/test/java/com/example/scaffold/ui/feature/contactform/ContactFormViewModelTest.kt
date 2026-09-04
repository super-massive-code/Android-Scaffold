package com.example.scaffold.ui.feature.contactform

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.R
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeContactRepository(
    private val saveError: Throwable? = null,
) : ContactRepository {
    val savedContacts = mutableListOf<Contact>()
    private val contactsFlow = MutableStateFlow<List<Contact>>(emptyList())

    override fun observeContacts(): Flow<List<Contact>> = contactsFlow.asStateFlow()

    override suspend fun saveContact(contact: Contact) {
        saveError?.let { throw it }
        savedContacts += contact
        contactsFlow.value = savedContacts.toList()
    }

    override suspend fun seedIfEmpty() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class ContactFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submitting a blank form reports required-field errors and does not save`() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(repository)

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.hasErrors)
            assertEquals(R.string.contact_form_error_required, state.firstNameError)
            assertEquals(R.string.contact_form_error_required, state.postcodeError)
            assertTrue(repository.savedContacts.isEmpty())
        }

    @Test
    fun `an invalid postcode is rejected without touching the repository`() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(repository)

            viewModel.onFirstNameChange("Ada")
            viewModel.onLastNameChange("Lovelace")
            viewModel.onAddressLine1Change("12 Analytical Engine Way")
            viewModel.onCityChange("London")
            viewModel.onPostcodeChange("NOTAPOSTCODE")
            viewModel.submit()
            advanceUntilIdle()

            assertEquals(R.string.contact_form_error_invalid_postcode, viewModel.uiState.value.postcodeError)
            assertTrue(repository.savedContacts.isEmpty())
        }

    @Test
    fun `a valid submission saves the contact and marks the form submitted`() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(repository)

            viewModel.onFirstNameChange("Ada")
            viewModel.onLastNameChange("Lovelace")
            viewModel.onAddressLine1Change("12 Analytical Engine Way")
            viewModel.onCityChange("London")
            viewModel.onPostcodeChange("sw1a 1aa")
            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.hasErrors)
            assertTrue(state.isSubmitted)
            assertNull(state.postcodeError)
            assertEquals(1, repository.savedContacts.size)
            assertEquals("SW1A 1AA", repository.savedContacts.single().postcode)
        }

    @Test
    fun `a failed save reports submitError and leaves the form on screen`() =
        runTest {
            val repository = FakeContactRepository(saveError = IllegalStateException("disk full"))
            val viewModel = ContactFormViewModel(repository)
            viewModel.fillInAValidContact()

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(R.string.contact_form_error_save_failed, state.submitError)
            assertFalse(state.isSubmitting)
            assertFalse(state.isSubmitted)
        }

    @Test
    fun `re-submitting clears the previous save failure`() =
        runTest {
            val repository = FakeContactRepository(saveError = IllegalStateException("disk full"))
            val viewModel = ContactFormViewModel(repository)
            viewModel.fillInAValidContact()
            viewModel.submit()
            advanceUntilIdle()

            // Blanking a field means the second submit stops at validation, so the stale save
            // failure has to have been cleared by submit() itself, not by a successful save.
            viewModel.onFirstNameChange("")
            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.submitError)
            assertEquals(R.string.contact_form_error_required, state.firstNameError)
        }
}

private fun ContactFormViewModel.fillInAValidContact() {
    onFirstNameChange("Ada")
    onLastNameChange("Lovelace")
    onAddressLine1Change("12 Kingsway")
    onCityChange("London")
    onPostcodeChange("SW1A 1AA")
}
