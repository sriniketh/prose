package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_data.models.BookInfoExport
import com.sriniketh.core_data.models.HighlightExport
import com.sriniketh.core_data.models.HighlightsExport
import com.sriniketh.core_platform.FileSource
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportHighlightsUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val highlightsRepository: HighlightsRepository,
    private val fileSource: FileSource
) {
    suspend operator fun invoke(bookId: String): Result<Uri> = try {
        val book = booksRepository.getBookByIdFromDb(bookId).getOrThrow()
        val highlights = highlightsRepository.getAllHighlightsForBookFromDb(bookId).first().getOrThrow()

        val export = HighlightsExport(
            id = book.id,
            info = BookInfoExport(
                title = book.info.title,
                subtitle = book.info.subtitle,
                authors = book.info.authors,
                thumbnailLink = book.info.thumbnailLink,
                publisher = book.info.publisher,
                publishedDate = book.info.publishedDate,
                description = book.info.description,
                pageCount = book.info.pageCount,
                averageRating = book.info.averageRating,
                ratingsCount = book.info.ratingsCount
            ),
            highlights = highlights.map { highlight ->
                HighlightExport(
                    id = highlight.id,
                    bookId = highlight.bookId,
                    text = highlight.text,
                    savedOnTimestamp = highlight.savedOnTimestamp
                )
            }
        )

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(HighlightsExport::class.java).indent("  ")
        val json = adapter.toJson(export)

        val uri = fileSource.writeToFile("highlights_export.json", json)
        Result.success(uri)
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
