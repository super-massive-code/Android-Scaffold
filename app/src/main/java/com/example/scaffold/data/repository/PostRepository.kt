package com.example.scaffold.data.repository

import com.example.scaffold.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<List<Post>>

    fun observePost(id: Int): Flow<Post?>

    suspend fun refresh()
}
