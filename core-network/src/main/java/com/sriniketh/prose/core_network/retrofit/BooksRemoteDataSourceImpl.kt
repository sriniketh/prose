package com.sriniketh.prose.core_network.retrofit

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.BuildConfig
import com.sriniketh.prose.core_network.model.Volumes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

class BooksRemoteDataSourceImpl @Inject constructor(
    remoteJson: Json
) : BooksRemoteDataSource {

    @OptIn(ExperimentalSerializationApi::class)
    private val booksApi: BooksApi = Retrofit.Builder()
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
        .addConverterFactory(remoteJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(BooksApi::class.java)

    override suspend fun getVolumes(searchQuery: String): Volumes =
        booksApi.volumes(searchQuery, BuildConfig.BOOKS_API_KEY, "lite")
}

private interface BooksApi {

    @GET("books/v1/volumes")
    suspend fun volumes(
        @Query("q") searchQuery: String,
        @Query("key") apiKey: String,
        @Query("projection") projection: String
    ): Volumes
}
