package com.example.scaffold.ui.feature.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.scaffold.data.repository.PostRepository
import com.example.scaffold.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class PostDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val postRepository: PostRepository,
    ) : ViewModel() {
        private val postId = savedStateHandle.toRoute<Destinations.PostDetail>().postId

        val uiState: StateFlow<PostDetailUiState> =
            postRepository
                .observePost(postId)
                .map { post ->
                    if (post != null) PostDetailUiState.Content(post) else PostDetailUiState.Loading
                }.catch { throwable -> emit(PostDetailUiState.Error(throwable.message)) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = PostDetailUiState.Loading,
                )

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                runCatching { postRepository.refresh() }
            }
        }
    }
