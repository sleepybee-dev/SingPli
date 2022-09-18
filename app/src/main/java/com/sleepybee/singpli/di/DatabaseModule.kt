package com.sleepybee.singpli.di

import android.content.Context
import com.sleepybee.singpli.database.SingPliDatabase
import com.sleepybee.singpli.database.SnippetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): SingPliDatabase {
        return SingPliDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideSnippetDao(database: SingPliDatabase): SnippetDao {
        return database.snippetDao()
    }
}