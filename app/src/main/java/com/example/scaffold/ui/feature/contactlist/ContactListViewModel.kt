package com.example.scaffold.ui.feature.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.toUiError
import com.example.scaffold.util.runSuspendCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class ContactListViewModel
    @Inject
    constructor(
        private val contactRepository: ContactRepository,
    ) : ViewModel() {
        private val recentlyDeleted = MutableStateFlow<Contact?>(null)

        val uiState: StateFlow<ContactListUiState> =
            combine<List<Contact>, Contact?, ContactListUiState>(
                contactRepository.observeContacts(),
                recentlyDeleted,
            ) { contacts, deleted ->
                ContactListUiState.Content(contacts = contacts, recentlyDeleted = deleted)
            }.catch { throwable -> emit(ContactListUiState.Error(throwable.toUiError())) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = ContactListUiState.Loading,
                )

        fun deleteContact(contact: Contact) {
            viewModelScope.launch {
                runSuspendCatching { contactRepository.deleteContact(contact) }
                    .onSuccess { recentlyDeleted.value = contact }
            }
        }

        /** Re-inserts the deleted contact with its original id, so nothing else has to change. */
        fun undoDelete() {
            val contact = recentlyDeleted.value ?: return
            recentlyDeleted.value = null
            viewModelScope.launch {
                runSuspendCatching { contactRepository.saveContact(contact) }
            }
        }

        /** Called once the undo snackbar has been dismissed without being acted on. */
        fun dismissUndo() {
            recentlyDeleted.value = null
        }
    }
