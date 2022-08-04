package com.sriniketh.core_models.book

data class Book(
    val id: String,
    val info: BookInfo
)

data class BookInfo(
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
