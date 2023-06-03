package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.model.ImageLinks
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.VolumeInfo
import com.sriniketh.prose.core_network.model.Volumes

class FakeBooksApi : BooksApi {

    var searchQueryPassed: String? = null
    var apiKeyPassed: String? = null
    var projectionPassed: String? = null
    override suspend fun volumes(searchQuery: String, apiKey: String, projection: String): Volumes {
        searchQueryPassed = searchQuery
        apiKeyPassed = apiKey
        projectionPassed = projection
        return Volumes(items = listOf(volume1(), volume2()))
    }

    var volumeIdPassed: String? = null
    override suspend fun volume(volumeId: String, apiKey: String): Volume {
        volumeIdPassed = volumeId
        apiKeyPassed = apiKey
        return volume1()
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

    private fun volume2() = Volume(
        id = "someId2", volumeInfo = VolumeInfo(
            title = "title 2",
            subtitle = "subtitle 2",
            description = "description 2",
            authors = listOf(
                "author 1", "author 2"
            ),
            imageLinks = ImageLinks(
                thumbnail = "thumbnail 2",
                smallThumbnail = null,
                small = null,
                medium = null,
                large = null
            ),
            publisher = "publisher 2",
            publishedDate = "published date 2",
            pageCount = 250,
            averageRating = 4.3,
            ratingsCount = 500
        )
    )
}