package com.example.scaffold.ui.feature.contactlist

import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.UiError

sealed interface ContactListUiState {
    data object Loading : ContactListUiState

    data class Error(
        val error: UiError,
    ) : ContactListUiState

    data class Content(
        val contacts: List<Contact>,
    ) : ContactListUiState
}
