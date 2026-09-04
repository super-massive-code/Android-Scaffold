package com.example.scaffold.ui.feature.contactlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffold.R
import com.example.scaffold.model.Contact
import com.example.scaffold.ui.components.EmptyState
import com.example.scaffold.ui.components.ErrorState
import com.example.scaffold.ui.components.LoadingIndicator
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

/** See `MealListScreen` for why each screen is a stateful wrapper over a stateless body. */
@Composable
fun ContactListScreen(
    onAddContactClick: () -> Unit,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ContactListScreen(
        uiState = uiState,
        onAddContactClick = onAddContactClick,
        onContactClick = onContactClick,
        onDeleteContact = viewModel::deleteContact,
        onUndoDelete = viewModel::undoDelete,
        onUndoDismissed = viewModel::dismissUndo,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun ContactListScreen(
    uiState: ContactListUiState,
    onAddContactClick: () -> Unit,
    onContactClick: (Long) -> Unit,
    onDeleteContact: (Contact) -> Unit,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val recentlyDeleted = (uiState as? ContactListUiState.Content)?.recentlyDeleted
    val deletedMessage = stringResource(R.string.contact_list_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(recentlyDeleted) {
        if (recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(message = deletedMessage, actionLabel = undoLabel)
            if (result == SnackbarResult.ActionPerformed) onUndoDelete() else onUndoDismissed()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_contacts)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContactClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.contact_list_action_add))
            }
        },
    ) { padding ->
        when (uiState) {
            ContactListUiState.Loading -> LoadingIndicator(Modifier.padding(padding))
            is ContactListUiState.Error ->
                ErrorState(
                    message = stringResource(uiState.error.messageRes),
                    modifier = Modifier.padding(padding),
                )
            is ContactListUiState.Content ->
                ContactList(
                    contacts = uiState.contacts,
                    onContactClick = onContactClick,
                    onDeleteContact = onDeleteContact,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun ContactList(
    contacts: List<Contact>,
    onContactClick: (Long) -> Unit,
    onDeleteContact: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (contacts.isEmpty()) {
        EmptyState(message = stringResource(R.string.contact_list_empty_message), modifier = modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(items = contacts, key = { it.id }) { contact ->
            SwipeToDeleteContact(onDelete = { onDeleteContact(contact) }) {
                ContactRow(contact = contact, onClick = { onContactClick(contact.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContact(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // `rememberSwipeToDismissBoxState` saves itself per LazyColumn item key, so a row that comes
    // back — an undone delete re-inserts the contact under its original id — is rebuilt with its
    // dismiss state still at EndToStart. Put it back in place first, then treat only *later*
    // transitions into that anchor as a delete; reading `currentValue` directly instead would
    // delete the contact again the instant undo restored it.
    LaunchedEffect(dismissState) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
        snapshotFlow { dismissState.currentValue }
            .drop(1)
            .filter { it == SwipeToDismissBoxValue.EndToStart }
            .collect { onDelete() }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { DeleteSwipeBackground() },
        content = { content() },
    )
}

@Composable
private fun DeleteSwipeBackground() {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp)
                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp),
    ) {
        Icon(
            Icons.Filled.Delete,
            contentDescription = stringResource(R.string.contact_list_action_delete),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier =
            modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.contact_list_full_name, contact.firstName, contact.lastName),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = contact.addressLine1, style = MaterialTheme.typography.bodyMedium)
            contact.addressLine2?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            Text(
                text = stringResource(R.string.contact_list_city_postcode, contact.city, contact.postcode),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
