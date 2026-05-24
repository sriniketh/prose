package com.sriniketh.prose.core_network.mapper

import com.sriniketh.prose.core_network.model.ImageLinks
import com.sriniketh.prose.core_network.model.OpenLibraryDoc
import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.VolumeInfo
import com.sriniketh.prose.core_network.model.Volumes

internal fun OpenLibrarySearchResponse.asVolumes(): Volumes =
    Volumes(items = docs.map { it.asVolume() })

internal fun OpenLibraryDoc.asVolume(description: String? = null): Volume =
    Volume(
        id = key.removePrefix("/works/"),
        volumeInfo = VolumeInfo(
            title = title,
            subtitle = subtitle,
            description = description,
            authors = authorName ?: emptyList(),
            imageLinks = coverId?.let { ImageLinks(thumbnail = coverImageUrl(it)) },
            publisher = publisher?.firstOrNull(),
            publishedDate = firstPublishYear?.toString(),
            pageCount = numberOfPagesMedian,
            averageRating = ratingsAverage,
            ratingsCount = ratingsCount
        )
    )

private fun coverImageUrl(coverId: Int): String =
    "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
