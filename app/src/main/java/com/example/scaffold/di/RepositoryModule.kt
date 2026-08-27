package com.example.scaffold.di

import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.data.repository.ContactRepositoryImpl
import com.example.scaffold.data.repository.PostRepository
import com.example.scaffold.data.repository.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository
}
