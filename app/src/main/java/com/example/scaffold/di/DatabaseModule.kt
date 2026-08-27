package com.example.scaffold.di

import android.content.Context
import androidx.room.Room
import com.example.scaffold.data.local.AppDatabase
import com.example.scaffold.data.local.ContactDao
import com.example.scaffold.data.local.MIGRATION_1_2
import com.example.scaffold.data.local.PostDao
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun providePostDao(appDatabase: AppDatabase): PostDao = appDatabase.postDao()

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: AppDatabase): ContactDao = appDatabase.contactDao()
}
