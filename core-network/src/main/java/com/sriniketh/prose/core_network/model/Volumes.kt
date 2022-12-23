package com.sriniketh.prose.core_network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Volumes(
    val items: List<Volume>
)

@JsonClass(generateAdapter = true)
data class Volume(
    val id: String,
    val volumeInfo: VolumeInfo
)

@JsonClass(generateAdapter = true)
data class VolumeInfo(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val authors: List<String>,
    val imageLinks: ImageLinks? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val averageRating: Double? = null,
    val ratingsCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ImageLinks(
    val thumbnail: String,
    val smallThumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)
