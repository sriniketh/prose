package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_models.book.Book

fun Book.asBookEntity(): BookEntity = BookEntity(
    id = id,
    title = info.title,
    subtitle = info.subtitle,
    authors = info.authors,
    thumbnailLink = info.thumbnailLink,
    publisher = info.publisher,
    publishedDate = info.publishedDate,
    description = info.description,
    pageCount = info.pageCount,
    averageRating = info.averageRating,
    ratingsCount = info.ratingsCount
)
