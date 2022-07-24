package com.sriniketh.prose.core_network.model

import com.sriniketh.core_models.search.Book
import com.sriniketh.core_models.search.BookSearchResult
import com.sriniketh.core_models.search.BookInfo
import kotlinx.serialization.Serializable

@Serializable
data class Volumes(
    val items: List<Volume>
)

@Serializable
data class Volume(
    val id: String,
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val authors: List<String>,
    val imageLinks: ImageLinks? = null
)

@Serializable
data class ImageLinks(
    val thumbnail: String,
    val smallThumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)

fun Volumes.asBookSearchResult() = BookSearchResult(
    items = items.map { it.asBook() }
)

fun Volume.asBook() = Book(
    id = id,
    info = volumeInfo.asBookInfo()
)

fun VolumeInfo.asBookInfo() = BookInfo(
    title = title,
    subtitle = subtitle,
    authors = authors,
    thumbnailLink = imageLinks?.thumbnail
)
