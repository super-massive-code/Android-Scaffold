package com.example.scaffold.ui.feature.postlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scaffold.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListViewModel
    @Inject
    constructor(
        private val postRepository: PostRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<PostListUiState>(PostListUiState.Loading)
        val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

        init {
            postRepository
                .observePosts()
                .onEach { posts ->
                    _uiState.update { current ->
                        when {
                            posts.isNotEmpty() -> PostListUiState.Content(posts)
                            current is PostListUiState.Error -> current
                            else -> PostListUiState.Loading
                        }
                    }
                }.launchIn(viewModelScope)
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                runCatching { postRepository.refresh() }
                    .onFailure { throwable ->
                        if (_uiState.value !is PostListUiState.Content) {
                            _uiState.value = PostListUiState.Error(throwable.message)
                        }
                    }
            }
        }
    }
