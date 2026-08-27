package com.example.scaffold.ui.feature.postlist

import com.example.scaffold.model.Post

sealed interface PostListUiState {
    data object Loading : PostListUiState

    data class Error(
        val message: String?,
    ) : PostListUiState

    data class Content(
        val posts: List<Post>,
    ) : PostListUiState
}
