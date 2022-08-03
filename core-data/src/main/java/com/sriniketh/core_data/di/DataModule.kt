package com.sriniketh.core_data.di

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_data.BooksRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun providesBooksRepository(
        booksRepositoryImpl: BooksRepositoryImpl
    ): BooksRepository = booksRepositoryImpl

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
