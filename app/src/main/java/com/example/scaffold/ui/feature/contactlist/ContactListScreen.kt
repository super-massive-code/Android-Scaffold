package com.example.scaffold.ui.feature.contactlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffold.R
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_contacts)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContactClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.contact_list_action_add))
            }
        },
    ) { padding ->
        when (val state = uiState) {
            ContactListUiState.Loading -> LoadingIndicator(Modifier.padding(padding))
            is ContactListUiState.Error ->
                ErrorState(
                    message = state.message ?: stringResource(R.string.contact_list_error_fallback),
                    modifier = Modifier.padding(padding),
                )
            is ContactListUiState.Content ->
                ContactList(contacts = state.contacts, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun ContactList(
    contacts: List<Contact>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(items = contacts, key = { it.id }) { contact ->
            ContactRow(contact = contact)
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.padding(vertical = 6.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${contact.firstName} ${contact.lastName}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = contact.addressLine1, style = MaterialTheme.typography.bodyMedium)
            contact.addressLine2?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            Text(text = "${contact.city} ${contact.postcode}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
