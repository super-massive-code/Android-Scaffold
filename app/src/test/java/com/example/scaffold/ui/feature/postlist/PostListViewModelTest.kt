package com.example.scaffold.ui.feature.postlist

import com.example.scaffold.MainDispatcherRule
import com.example.scaffold.data.repository.PostRepository
import com.example.scaffold.model.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakePostRepository(
    private val postsAfterRefresh: List<Post> = emptyList(),
    private val refreshError: Throwable? = null,
) : PostRepository {
    private val postsFlow = MutableStateFlow<List<Post>>(emptyList())

    override fun observePosts(): Flow<List<Post>> = postsFlow.asStateFlow()

    override fun observePost(id: Int): Flow<Post?> = postsFlow.map { list -> list.find { it.id == id } }

    override suspend fun refresh() {
        refreshError?.let { throw it }
        postsFlow.value = postsAfterRefresh
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PostListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits content once refresh populates the cache`() =
        runTest {
            val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
            val viewModel = PostListViewModel(FakePostRepository(postsAfterRefresh = listOf(post)))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is PostListUiState.Content)
            assertEquals(listOf(post), (state as PostListUiState.Content).posts)
        }

    @Test
    fun `emits error when refresh fails and the cache is empty`() =
        runTest {
            val viewModel = PostListViewModel(FakePostRepository(refreshError = IllegalStateException("boom")))

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is PostListUiState.Error)
            assertEquals("boom", (state as PostListUiState.Error).message)
        }
}
