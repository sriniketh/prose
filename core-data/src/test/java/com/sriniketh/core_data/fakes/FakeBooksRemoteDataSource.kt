package com.sriniketh.core_data.fakes

import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.model.ImageLinks
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.VolumeInfo
import com.sriniketh.prose.core_network.model.Volumes

class FakeBooksRemoteDataSource : BooksRemoteDataSource {

    var shouldGetVolumesThrowException = false
    var searchQueryPassed: String? = null
    override suspend fun getVolumes(searchQuery: String): Volumes {
        searchQueryPassed = searchQuery
        return if (shouldGetVolumesThrowException) throw RuntimeException("some error fetching volumes")
        else Volumes(items = listOf(volume1()))
    }

    var shouldGetVolumeThrowException = false
    var volumeIdPassed: String? = null
    override suspend fun getVolume(volumeId: String): Volume {
        volumeIdPassed = volumeId
        return if (shouldGetVolumeThrowException) throw RuntimeException("some error fetching volume")
        else volume1()
    }

    private fun volume1() = Volume(
        id = "someId1", volumeInfo = VolumeInfo(
            title = "title 1",
            subtitle = "subtitle 1",
            description = "description 1",
            authors = listOf(
                "author 1", "author 2"
            ),
            imageLinks = ImageLinks(
                thumbnail = "thumbnail 1",
                smallThumbnail = null,
                small = null,
                medium = null,
                large = null
            ),
            publisher = "publisher 1",
            publishedDate = "published date 1",
            pageCount = 100,
            averageRating = 4.5,
            ratingsCount = 4500
        )
    )
}