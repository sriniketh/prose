package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import com.sriniketh.prose.core_network.model.OpenLibraryWork
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BooksApi {

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String,
        @Query("limit") limit: Int
    ): OpenLibrarySearchResponse

    @GET("works/{id}.json")
    suspend fun work(
        @Path("id") workId: String
    ): OpenLibraryWork
}
