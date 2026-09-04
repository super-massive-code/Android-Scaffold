package com.example.scaffold.ui.feature.contactform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.scaffold.R
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.navigation.Destinations
import com.example.scaffold.util.runSuspendCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactFormViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val contactRepository: ContactRepository,
    ) : ViewModel() {
        private val contactId = savedStateHandle.toRoute<Destinations.ContactForm>().contactId
        private val isEditing = contactId != Destinations.NEW_CONTACT

        private val _uiState = MutableStateFlow(ContactFormUiState(isEditing = isEditing))
        val uiState: StateFlow<ContactFormUiState> = _uiState.asStateFlow()

        init {
            if (isEditing) {
                viewModelScope.launch {
                    contactRepository.observeContact(contactId).first()?.let { contact ->
                        _uiState.update { it.withValuesFrom(contact) }
                    }
                }
            }
        }

        fun onFirstNameChange(value: String) = _uiState.update { it.copy(firstName = value, firstNameError = null) }

        fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value, lastNameError = null) }

        fun onAddressLine1Change(value: String) =
            _uiState.update { it.copy(addressLine1 = value, addressLine1Error = null) }

        fun onAddressLine2Change(value: String) = _uiState.update { it.copy(addressLine2 = value) }

        fun onCityChange(value: String) = _uiState.update { it.copy(city = value, cityError = null) }

        fun onPostcodeChange(value: String) = _uiState.update { it.copy(postcode = value, postcodeError = null) }

        fun submit() {
            val validated =
                _uiState.updateAndGet {
                    it.copy(
                        firstNameError = requiredFieldError(it.firstName),
                        lastNameError = requiredFieldError(it.lastName),
                        addressLine1Error = requiredFieldError(it.addressLine1),
                        cityError = requiredFieldError(it.city),
                        postcodeError = postcodeError(it.postcode),
                        submitError = null,
                    )
                }
            if (validated.hasErrors) return

            _uiState.update { it.copy(isSubmitting = true) }
            viewModelScope.launch {
                runSuspendCatching {
                    val contact = validated.toContact(contactId)
                    if (isEditing) contactRepository.updateContact(contact) else contactRepository.saveContact(contact)
                }.onSuccess { _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) } }
                    .onFailure {
                        _uiState.update {
                            it.copy(isSubmitting = false, submitError = R.string.contact_form_error_save_failed)
                        }
                    }
            }
        }
    }

private fun ContactFormUiState.withValuesFrom(contact: Contact): ContactFormUiState =
    copy(
        firstName = contact.firstName,
        lastName = contact.lastName,
        addressLine1 = contact.addressLine1,
        addressLine2 = contact.addressLine2.orEmpty(),
        city = contact.city,
        postcode = contact.postcode,
    )

private fun ContactFormUiState.toContact(id: Long): Contact =
    Contact(
        id = id,
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        addressLine1 = addressLine1.trim(),
        addressLine2 = addressLine2.trim().ifBlank { null },
        city = city.trim(),
        postcode = postcode.trim().uppercase(),
    )
