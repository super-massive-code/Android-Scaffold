package com.example.scaffold.ui.feature.contactform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scaffold.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactFormScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = { ContactFormTopBar(onBack = onBack) },
    ) { padding ->
        ContactFormFields(
            uiState = uiState,
            onFirstNameChange = viewModel::onFirstNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onAddressLine1Change = viewModel::onAddressLine1Change,
            onAddressLine2Change = viewModel::onAddressLine2Change,
            onCityChange = viewModel::onCityChange,
            onPostcodeChange = viewModel::onPostcodeChange,
            onSubmit = viewModel::submit,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactFormTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.contact_form_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        },
    )
}

@Suppress("LongParameterList")
@Composable
private fun ContactFormFields(
    uiState: ContactFormUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onAddressLine1Change: (String) -> Unit,
    onAddressLine2Change: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPostcodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = onFirstNameChange,
            label = { Text(stringResource(R.string.contact_form_label_first_name)) },
            isError = uiState.firstNameError != null,
            supportingText = uiState.firstNameError?.let { { Text(stringResource(it)) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = onLastNameChange,
            label = { Text(stringResource(R.string.contact_form_label_last_name)) },
            isError = uiState.lastNameError != null,
            supportingText = uiState.lastNameError?.let { { Text(stringResource(it)) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.addressLine1,
            onValueChange = onAddressLine1Change,
            label = { Text(stringResource(R.string.contact_form_label_address_line1)) },
            isError = uiState.addressLine1Error != null,
            supportingText = uiState.addressLine1Error?.let { { Text(stringResource(it)) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.addressLine2,
            onValueChange = onAddressLine2Change,
            label = { Text(stringResource(R.string.contact_form_label_address_line2)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.city,
            onValueChange = onCityChange,
            label = { Text(stringResource(R.string.contact_form_label_city)) },
            isError = uiState.cityError != null,
            supportingText = uiState.cityError?.let { { Text(stringResource(it)) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.postcode,
            onValueChange = onPostcodeChange,
            label = { Text(stringResource(R.string.contact_form_label_postcode)) },
            isError = uiState.postcodeError != null,
            supportingText = uiState.postcodeError?.let { { Text(stringResource(it)) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onSubmit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val saveLabelRes =
                if (uiState.isSubmitting) {
                    R.string.contact_form_action_saving
                } else {
                    R.string.contact_form_action_save
                }
            Text(stringResource(saveLabelRes))
        }
    }
}
