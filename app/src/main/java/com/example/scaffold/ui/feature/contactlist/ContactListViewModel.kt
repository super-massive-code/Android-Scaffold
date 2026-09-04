package com.example.scaffold.ui.feature.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class ContactListViewModel
    @Inject
    constructor(
        contactRepository: ContactRepository,
    ) : ViewModel() {
        val uiState: StateFlow<ContactListUiState> =
            contactRepository
                .observeContacts()
                .map<List<Contact>, ContactListUiState> { contacts -> ContactListUiState.Content(contacts) }
                .catch { throwable -> emit(ContactListUiState.Error(throwable.toUiError())) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = ContactListUiState.Loading,
                )
    }
