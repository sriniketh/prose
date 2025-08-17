package com.sriniketh.feature_viewhighlights.fakes

import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeHighlightsRepository : HighlightsRepository {

    var shouldGetAllHighlightsForBookFromDbThrowException = false
    var shouldDeleteHighlightFromDbThrowException = false
    var bookIdPassed: String? = null
    var deletedHighlight: Highlight? = null

    private val fakeHighlight = Highlight(
        id = "test-highlight-id",
        bookId = "test-book-id",
        text = "Test highlight text",
        savedOnTimestamp = "2023-01-01 12:00 PM"
    )

    override suspend fun insertHighlightIntoDb(highlight: Highlight): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun loadHighlightFromDb(highlightId: String): Result<Highlight> {
        return Result.success(fakeHighlight)
    }

    override fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>> = flow {
        bookIdPassed = bookId
        if (shouldGetAllHighlightsForBookFromDbThrowException) {
            emit(Result.failure(RuntimeException("Get all highlights failed")))
        } else {
            emit(Result.success(listOf(fakeHighlight)))
        }
    }

    override suspend fun deleteHighlightFromDb(highlight: Highlight): Result<Unit> {
        deletedHighlight = highlight
        return if (shouldDeleteHighlightFromDbThrowException) {
            Result.failure(RuntimeException("Delete highlight failed"))
        } else {
            Result.success(Unit)
        }
    }
}