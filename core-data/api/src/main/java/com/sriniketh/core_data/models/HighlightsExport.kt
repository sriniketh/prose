package com.sriniketh.core_data.models

import kotlinx.serialization.Serializable

@Serializable
data class HighlightsExport(
    val id: String,
    val info: BookInfoExport,
    val highlights: List<HighlightExport>
)

@Serializable
data class BookInfoExport(
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?,
    val averageRating: Double?,
    val ratingsCount: Int?
)

@Serializable
data class HighlightExport(
    val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
