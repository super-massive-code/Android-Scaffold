package com.example.scaffold.data.repository

import com.example.scaffold.data.local.PostDao
import com.example.scaffold.data.remote.PostApi
import com.example.scaffold.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepositoryImpl
    @Inject
    constructor(
        private val postApi: PostApi,
        private val postDao: PostDao,
    ) : PostRepository {
        override fun observePosts(): Flow<List<Post>> =
            postDao.observePosts().map { entities -> entities.map { it.toDomain() } }

        override fun observePost(id: Int): Flow<Post?> = postDao.observePost(id).map { it?.toDomain() }

        override suspend fun refresh() {
            val posts = postApi.getPosts()
            postDao.upsertAll(posts.map { it.toEntity() })
        }
    }
