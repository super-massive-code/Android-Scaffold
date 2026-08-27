package com.example.scaffold.data.repository

import com.example.scaffold.data.local.PostEntity
import com.example.scaffold.data.remote.dto.PostDto
import com.example.scaffold.model.Post

fun PostDto.toEntity(): PostEntity = PostEntity(id = id, userId = userId, title = title, body = body)

fun PostEntity.toDomain(): Post = Post(id = id, userId = userId, title = title, body = body)
