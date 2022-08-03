package com.sriniketh.prose.core_network.model

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
