package com.example.scaffold.data.repository

import com.example.scaffold.data.local.PostDao
import com.example.scaffold.data.local.PostEntity
import com.example.scaffold.data.remote.PostApi
import com.example.scaffold.data.remote.dto.PostDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakePostApi(
    private val posts: List<PostDto>,
) : PostApi {
    override suspend fun getPosts(): List<PostDto> = posts
}

private class FakePostDao : PostDao {
    private val table = MutableStateFlow<List<PostEntity>>(emptyList())

    override fun observePosts(): Flow<List<PostEntity>> = table.asStateFlow()

    override fun observePost(id: Int): Flow<PostEntity?> = table.map { list -> list.find { it.id == id } }

    override suspend fun upsertAll(posts: List<PostEntity>) {
        val untouched = table.value.filterNot { existing -> posts.any { it.id == existing.id } }
        table.value = untouched + posts
    }
}

class PostRepositoryImplTest {
    @Test
    fun `refresh fetches from the api and caches the result`() =
        runTest {
            val dto = PostDto(id = 1, userId = 1, title = "Title", body = "Body")
            val repository: PostRepository = PostRepositoryImpl(FakePostApi(listOf(dto)), FakePostDao())

            repository.refresh()

            val posts = repository.observePosts().first()
            assertEquals(1, posts.size)
            assertEquals("Title", posts.first().title)
        }

    @Test
    fun `observePost maps the matching cached entity to a domain model`() =
        runTest {
            val dto = PostDto(id = 5, userId = 2, title = "Five", body = "Body five")
            val repository: PostRepository = PostRepositoryImpl(FakePostApi(listOf(dto)), FakePostDao())

            repository.refresh()

            val post = repository.observePost(5).first()
            assertEquals("Five", post?.title)
        }
}
