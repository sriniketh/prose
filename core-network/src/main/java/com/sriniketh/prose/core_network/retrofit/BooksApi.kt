package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.Volumes
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BooksApi {

    @GET("books/v1/volumes")
    suspend fun volumes(
        @Query("q") searchQuery: String,
        @Query("key") apiKey: String,
        @Query("projection") projection: String
    ): Volumes

    @GET("books/v1/volumes/{id}")
    suspend fun volume(
        @Path("id") volumeId: String,
        @Query("key") apiKey: String
    ): Volume
}
