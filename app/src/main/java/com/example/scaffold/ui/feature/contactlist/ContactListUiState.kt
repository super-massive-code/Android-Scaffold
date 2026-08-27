package com.example.scaffold.ui.feature.contactlist

import com.example.scaffold.model.Contact

sealed interface ContactListUiState {
    data object Loading : ContactListUiState

    data class Error(
        val message: String?,
    ) : ContactListUiState

    data class Content(
        val contacts: List<Contact>,
    ) : ContactListUiState
}
