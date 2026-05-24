package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.mapper.asVolume
import com.sriniketh.prose.core_network.mapper.asVolumes
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.Volumes
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

private const val SEARCH_FIELDS =
    "key,title,subtitle,author_name,cover_i,first_publish_year," +
        "number_of_pages_median,publisher,ratings_average,ratings_count"
private const val SEARCH_LIMIT = 20

class BooksRemoteDataSourceImpl @Inject constructor(
    private val booksApi: BooksApi
) : BooksRemoteDataSource {

    override suspend fun getVolumes(searchQuery: String): Volumes =
        booksApi.search(searchQuery, SEARCH_FIELDS, SEARCH_LIMIT).asVolumes()

    override suspend fun getVolume(volumeId: String): Volume {
        val doc = booksApi.search("key:/works/$volumeId", SEARCH_FIELDS, limit = 1)
            .docs.firstOrNull()
            ?: throw NoSuchElementException("No work found for id: $volumeId")
        return doc.asVolume(fetchDescription(volumeId))
    }

    private suspend fun fetchDescription(volumeId: String): String? =
        try {
            booksApi.work(volumeId).description?.value
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            null
        }
}
