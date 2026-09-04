package com.example.scaffold.ui.feature.contactlist

import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.UiError

sealed interface ContactListUiState {
    data object Loading : ContactListUiState

    data class Error(
        val error: UiError,
    ) : ContactListUiState

    /**
     * [recentlyDeleted] is the contact a swipe just removed, waiting on an undo snackbar —
     * the same state-field-not-event-channel pattern as `MealListUiState.Content`'s
     * `transientError`.
     */
    data class Content(
        val contacts: List<Contact>,
        val recentlyDeleted: Contact? = null,
    ) : ContactListUiState
}
