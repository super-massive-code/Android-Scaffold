package com.example.scaffold.ui.feature.contactform

import androidx.annotation.StringRes

/**
 * Unlike a read screen's Loading/Error/Content sealed interface, a form has
 * no resource-loading lifecycle — it's a flat bag of field values, per-field
 * validation errors, and submission status, all updated in place.
 */
data class ContactFormUiState(
    val firstName: String = "",
    val lastName: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val postcode: String = "",
    @param:StringRes val firstNameError: Int? = null,
    @param:StringRes val lastNameError: Int? = null,
    @param:StringRes val addressLine1Error: Int? = null,
    @param:StringRes val cityError: Int? = null,
    @param:StringRes val postcodeError: Int? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
) {
    val hasErrors: Boolean
        get() =
            listOf(firstNameError, lastNameError, addressLine1Error, cityError, postcodeError)
                .any { it != null }
}
