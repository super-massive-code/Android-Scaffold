package com.example.scaffold.ui.feature.postdetail

import com.example.scaffold.model.Post

sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState

    data class Error(
        val message: String?,
    ) : PostDetailUiState

    data class Content(
        val post: Post,
    ) : PostDetailUiState
}
