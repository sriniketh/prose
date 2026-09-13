package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_data.models.BookInfoExport
import com.sriniketh.core_data.models.HighlightExport
import com.sriniketh.core_data.models.HighlightsExport
import com.sriniketh.core_platform.FileSource
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
private val exportJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    explicitNulls = false
}

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

        val json = exportJson.encodeToString(export)

        val fileName = "${book.info.title.lowercase().replace(" ", "_")}_export.json"
        val uri = fileSource.writeToFile(fileName, json)
        Result.success(uri)
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
