package com.example.scaffold.ui.feature.contactform

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.scaffold.R
import com.example.scaffold.ui.theme.ScaffoldTheme

private val PreviewFilledInState =
    ContactFormUiState(
        firstName = "Ada",
        lastName = "Lovelace",
        addressLine1 = "12 Kingsway",
        city = "London",
        postcode = "WC2B 6NH",
    )

@PreviewLightDark
@Composable
private fun ContactFormScreenEmptyPreview() {
    PreviewContactForm(ContactFormUiState())
}

@PreviewLightDark
@Composable
private fun ContactFormScreenFilledInPreview() {
    PreviewContactForm(PreviewFilledInState)
}

@PreviewLightDark
@Composable
private fun ContactFormScreenFieldErrorsPreview() {
    PreviewContactForm(
        ContactFormUiState(
            firstName = "Ada",
            postcode = "NOT A POSTCODE",
            lastNameError = R.string.contact_form_error_required,
            addressLine1Error = R.string.contact_form_error_required,
            cityError = R.string.contact_form_error_required,
            postcodeError = R.string.contact_form_error_invalid_postcode,
        ),
    )
}

@PreviewLightDark
@Composable
private fun ContactFormScreenSubmitFailurePreview() {
    PreviewContactForm(PreviewFilledInState.copy(submitError = R.string.contact_form_error_save_failed))
}

@PreviewLightDark
@Composable
private fun ContactFormScreenSubmittingPreview() {
    PreviewContactForm(PreviewFilledInState.copy(isSubmitting = true))
}

@Composable
private fun PreviewContactForm(uiState: ContactFormUiState) {
    ScaffoldTheme {
        ContactFormScreen(
            uiState = uiState,
            onBack = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onAddressLine1Change = {},
            onAddressLine2Change = {},
            onCityChange = {},
            onPostcodeChange = {},
            onSubmit = {},
        )
    }
}
