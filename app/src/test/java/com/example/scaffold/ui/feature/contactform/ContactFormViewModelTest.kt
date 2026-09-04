package com.example.scaffold.ui.feature.contactform

import androidx.lifecycle.SavedStateHandle
import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.R
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.navigation.Destinations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private class FakeContactRepository(
    initialContacts: List<Contact> = emptyList(),
    private val saveError: Throwable? = null,
) : ContactRepository {
    val savedContacts = mutableListOf<Contact>()
    val updatedContacts = mutableListOf<Contact>()
    private val contactsFlow = MutableStateFlow(initialContacts)

    override fun observeContacts(): Flow<List<Contact>> = contactsFlow.asStateFlow()

    override fun observeContact(id: Long): Flow<Contact?> =
        contactsFlow.map { contacts -> contacts.find { it.id == id } }

    override suspend fun saveContact(contact: Contact) {
        saveError?.let { throw it }
        savedContacts += contact
        contactsFlow.value = contactsFlow.value + contact
    }

    override suspend fun updateContact(contact: Contact) {
        saveError?.let { throw it }
        updatedContacts += contact
    }

    override suspend fun deleteContact(contact: Contact) {
        contactsFlow.value = contactsFlow.value - contact
    }

    override suspend fun seedIfEmpty() = Unit
}

/**
 * Instrumented for the same reason as `MealDetailViewModelTest`: [ContactFormViewModel] now
 * reads its `contactId` with `SavedStateHandle.toRoute<Destinations.ContactForm>()`, which goes
 * through `android.os.Bundle` and can't run on the JVM. The form's validation rules — the part
 * with the most cases — stay in the fast JVM suite as `ContactFormValidationTest`, because they
 * are plain functions that never touch a nav argument.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ContactFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submittingABlankFormReportsRequiredFieldErrorsAndDoesNotSave() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(newContactHandle(), repository)

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.hasErrors)
            assertEquals(R.string.contact_form_error_required, state.firstNameError)
            assertEquals(R.string.contact_form_error_required, state.postcodeError)
            assertTrue(repository.savedContacts.isEmpty())
        }

    @Test
    fun anInvalidPostcodeIsRejectedWithoutTouchingTheRepository() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(newContactHandle(), repository)

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
    fun aValidSubmissionSavesTheContactAndMarksTheFormSubmitted() =
        runTest {
            val repository = FakeContactRepository()
            val viewModel = ContactFormViewModel(newContactHandle(), repository)

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
    fun aFailedSaveReportsSubmitErrorAndLeavesTheFormOnScreen() =
        runTest {
            val repository = FakeContactRepository(saveError = IllegalStateException("disk full"))
            val viewModel = ContactFormViewModel(newContactHandle(), repository)
            viewModel.fillInAValidContact()

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(R.string.contact_form_error_save_failed, state.submitError)
            assertFalse(state.isSubmitting)
            assertFalse(state.isSubmitted)
        }

    @Test
    fun resubmittingClearsThePreviousSaveFailure() =
        runTest {
            val repository = FakeContactRepository(saveError = IllegalStateException("disk full"))
            val viewModel = ContactFormViewModel(newContactHandle(), repository)
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

    @Test
    fun editModePreloadsTheContactsFieldsAndUpdatesInsteadOfInserting() =
        runTest {
            val existing =
                Contact(
                    id = 7,
                    firstName = "Grace",
                    lastName = "Hopper",
                    addressLine1 = "45 Harbour Road",
                    addressLine2 = null,
                    city = "Portsmouth",
                    postcode = "PO1 3AX",
                )
            val repository = FakeContactRepository(initialContacts = listOf(existing))
            val viewModel = ContactFormViewModel(SavedStateHandle(mapOf("contactId" to 7L)), repository)
            advanceUntilIdle()

            val preloaded = viewModel.uiState.value
            assertTrue(preloaded.isEditing)
            assertEquals("Grace", preloaded.firstName)
            assertEquals("PO1 3AX", preloaded.postcode)

            viewModel.onCityChange("Southsea")
            viewModel.submit()
            advanceUntilIdle()

            assertTrue(repository.savedContacts.isEmpty())
            assertEquals(existing.copy(city = "Southsea"), repository.updatedContacts.single())
        }
}

private fun ContactFormViewModel.fillInAValidContact() {
    onFirstNameChange("Ada")
    onLastNameChange("Lovelace")
    onAddressLine1Change("12 Kingsway")
    onCityChange("London")
    onPostcodeChange("SW1A 1AA")
}

private fun newContactHandle() = SavedStateHandle(mapOf("contactId" to Destinations.NEW_CONTACT))
