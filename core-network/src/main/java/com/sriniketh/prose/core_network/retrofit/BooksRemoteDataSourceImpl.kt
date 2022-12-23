package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.BuildConfig
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.Volumes
import javax.inject.Inject

class BooksRemoteDataSourceImpl @Inject constructor(
    private val booksApi: BooksApi
) : BooksRemoteDataSource {

    override suspend fun getVolumes(searchQuery: String): Volumes =
        booksApi.volumes(searchQuery, BuildConfig.BOOKS_API_KEY, "lite")

    override suspend fun getVolume(volumeId: String): Volume =
        booksApi.volume(volumeId, BuildConfig.BOOKS_API_KEY)
}
