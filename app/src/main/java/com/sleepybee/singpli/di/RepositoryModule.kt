package com.sleepybee.singpli.di

import com.sleepybee.singpli.database.FirebaseRepository
import com.sleepybee.singpli.database.SnippetDao
import com.sleepybee.singpli.database.YTSnippetRepository
import com.sleepybee.singpli.network.YTService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideYTSnippetRepository(
        ytService: YTService,
        snippetDao: SnippetDao
    ) : YTSnippetRepository {
        return YTSnippetRepository(ytService, snippetDao)
    }

    @Provides
    @ViewModelScoped
    fun provideFirebaseRepository(
    ) : FirebaseRepository {
        return FirebaseRepository()
    }
}