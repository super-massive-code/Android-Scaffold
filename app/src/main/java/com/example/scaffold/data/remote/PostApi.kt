package com.example.scaffold.data.remote

import com.example.scaffold.data.remote.dto.PostDto
import retrofit2.http.GET

interface PostApi {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
