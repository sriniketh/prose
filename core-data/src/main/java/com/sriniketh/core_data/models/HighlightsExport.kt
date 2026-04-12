package com.sriniketh.core_data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HighlightsExport(
    val id: String,
    val info: BookInfoExport,
    val highlights: List<HighlightExport>
)

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
data class HighlightExport(
    val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
