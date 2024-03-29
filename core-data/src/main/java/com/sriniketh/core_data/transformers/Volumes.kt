package com.sriniketh.core_data.transformers

import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_models.search.BookSearch
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.VolumeInfo
import com.sriniketh.prose.core_network.model.Volumes

fun Volumes.asBookSearchResult() = BookSearch(
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
    thumbnailLink = imageLinks?.thumbnail,
    publisher = publisher,
    publishedDate = publishedDate,
    description = description,
    pageCount = pageCount,
    averageRating = averageRating,
    ratingsCount = ratingsCount
)
