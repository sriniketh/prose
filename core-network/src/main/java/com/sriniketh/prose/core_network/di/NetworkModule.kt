package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.BuildConfig
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.retrofit.BooksApi
import com.sriniketh.prose.core_network.retrofit.BooksRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
        .baseUrl("https://openlibrary.org/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", "Prose Android App")
                            .build()
                    )
                }
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                            else HttpLoggingInterceptor.Level.NONE
                        )
                    }
                )
                .build()
        )
        .addConverterFactory(booksApiJson.asConverterFactory("application/json; charset=utf-8".toMediaType()))
        .build()
        .create(BooksApi::class.java)
}
