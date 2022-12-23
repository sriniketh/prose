package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.retrofit.BooksApi
import com.sriniketh.prose.core_network.retrofit.BooksRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    fun providesRemoteJson(): BooksApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                )
                .build()
        )
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(BooksApi::class.java)
}
