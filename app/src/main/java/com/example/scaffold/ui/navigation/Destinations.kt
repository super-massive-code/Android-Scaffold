package com.example.scaffold.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object ContactList : Destinations

    @Serializable
    data object PostList : Destinations

    @Serializable
    data class PostDetail(
        val postId: Int,
    ) : Destinations

    @Serializable
    data object ContactForm : Destinations
}
