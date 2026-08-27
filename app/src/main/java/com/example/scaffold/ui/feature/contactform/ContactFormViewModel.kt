package com.example.scaffold.ui.feature.contactform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactFormViewModel
    @Inject
    constructor(
        private val contactRepository: ContactRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ContactFormUiState())
        val uiState: StateFlow<ContactFormUiState> = _uiState.asStateFlow()

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
                    )
                }
            if (validated.hasErrors) return

            _uiState.update { it.copy(isSubmitting = true) }
            viewModelScope.launch {
                contactRepository.saveContact(validated.toContact())
                _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
            }
        }
    }

private fun ContactFormUiState.toContact(): Contact =
    Contact(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        addressLine1 = addressLine1.trim(),
        addressLine2 = addressLine2.trim().ifBlank { null },
        city = city.trim(),
        postcode = postcode.trim().uppercase(),
    )
