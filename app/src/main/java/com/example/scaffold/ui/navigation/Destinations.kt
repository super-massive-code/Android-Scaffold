package com.example.scaffold.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object ContactList : Destinations

    @Serializable
    data object MealList : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data class MealDetail(
        val mealId: String,
    ) : Destinations

    /**
     * [contactId] is [NEW_CONTACT] when the form is opened to create a contact. It's a
     * sentinel rather than a nullable `Long?` because type-safe routes have no `NavType` for
     * a nullable primitive; 0 is safe as "no contact" because Room's `autoGenerate` ids start
     * at 1, and it's already what `Contact.id` defaults to before a row is written.
     */
    @Serializable
    data class ContactForm(
        val contactId: Long = NEW_CONTACT,
    ) : Destinations

    companion object {
        const val NEW_CONTACT: Long = 0L
    }
}
