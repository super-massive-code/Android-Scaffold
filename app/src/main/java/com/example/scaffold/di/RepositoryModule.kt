package com.example.scaffold.di

import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.data.repository.ContactRepositoryImpl
import com.example.scaffold.data.repository.MealRepository
import com.example.scaffold.data.repository.MealRepositoryImpl
import com.example.scaffold.data.repository.UserPreferencesRepository
import com.example.scaffold.data.repository.UserPreferencesRepositoryImpl
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
    abstract fun bindMealRepository(impl: MealRepositoryImpl): MealRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
