package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo

fun BookEntity.asBook() = Book(
    id = id,
    info = BookInfo(
        title = title,
        subtitle = subtitle,
        authors = authors,
        thumbnailLink = thumbnailLink,
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        pageCount = pageCount,
        averageRating = averageRating,
        ratingsCount = ratingsCount
    )
)
