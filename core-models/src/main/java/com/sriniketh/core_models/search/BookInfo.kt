package com.sriniketh.core_models.search

data class BookSearch(
    val items: List<Book>
)

data class Book(
    val id: String,
    val info: BookInfo
)

data class BookInfo(
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?
)
