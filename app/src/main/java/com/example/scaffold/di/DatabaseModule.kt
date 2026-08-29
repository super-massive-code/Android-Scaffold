package com.example.scaffold.di

import android.content.Context
import androidx.room.Room
import com.example.scaffold.data.local.AppDatabase
import com.example.scaffold.data.local.ContactDao
import com.example.scaffold.data.local.MealDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "scaffold.db")
            .build()

    @Provides
    @Singleton
    fun provideMealDao(appDatabase: AppDatabase): MealDao = appDatabase.mealDao()

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: AppDatabase): ContactDao = appDatabase.contactDao()
}
