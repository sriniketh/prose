package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.retrofit.BooksRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun providesBooksRemoteDataSource(
        booksRemoteDataSourceImpl: BooksRemoteDataSourceImpl
    ): BooksRemoteDataSource = booksRemoteDataSourceImpl

    @Provides
    @Singleton
    fun providesRemoteJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}
