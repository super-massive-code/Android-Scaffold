package com.example.scaffold.ui.feature.contactlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.UiError
import com.example.scaffold.ui.theme.ScaffoldTheme

private val PreviewContacts =
    listOf(
        Contact(
            id = 1,
            firstName = "Ada",
            lastName = "Lovelace",
            addressLine1 = "12 Kingsway",
            addressLine2 = null,
            city = "London",
            postcode = "WC2B 6NH",
        ),
        Contact(
            id = 2,
            firstName = "Alan",
            lastName = "Turing",
            addressLine1 = "Flat 4",
            addressLine2 = "78 Wilmslow Road",
            city = "Manchester",
            postcode = "M14 5TQ",
        ),
    )

@PreviewLightDark
@Composable
private fun ContactListScreenContentPreview() {
    PreviewContactList(ContactListUiState.Content(PreviewContacts))
}

@PreviewLightDark
@Composable
private fun ContactListScreenEmptyPreview() {
    PreviewContactList(ContactListUiState.Content(emptyList()))
}

@PreviewLightDark
@Composable
private fun ContactListScreenLoadingPreview() {
    PreviewContactList(ContactListUiState.Loading)
}

@PreviewLightDark
@Composable
private fun ContactListScreenErrorPreview() {
    PreviewContactList(ContactListUiState.Error(UiError.Unknown))
}

@Composable
private fun PreviewContactList(uiState: ContactListUiState) {
    ScaffoldTheme {
        ContactListScreen(
            uiState = uiState,
            onAddContactClick = {},
            onContactClick = {},
            onDeleteContact = {},
            onUndoDelete = {},
            onUndoDismissed = {},
        )
    }
}
