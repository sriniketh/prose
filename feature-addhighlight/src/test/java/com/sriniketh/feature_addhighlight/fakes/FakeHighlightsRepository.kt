package com.sriniketh.feature_addhighlight.fakes

import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeHighlightsRepository : HighlightsRepository {

    var shouldGetAllHighlightsForBookFromDbThrowException = false
    var shouldDeleteHighlightFromDbThrowException = false
    var shouldInsertHighlightIntoDbThrowException = false
    var shouldLoadHighlightFromDbThrowException = false

    var insertedHighlight: Highlight? = null
    var loadedHighlightId: String? = null

    private val fakeHighlight = Highlight(
        id = "test-highlight-id",
        bookId = "test-book-id",
        text = "Test highlight text",
        savedOnTimestamp = "2023-01-01"
    )

    override fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>> = flow {
        if (shouldGetAllHighlightsForBookFromDbThrowException) {
            emit(Result.failure(RuntimeException("Get all highlights failed")))
        } else {
            emit(Result.success(listOf(fakeHighlight)))
        }
    }

    override suspend fun deleteHighlightFromDb(highlight: Highlight): Result<Unit> {
        return if (shouldDeleteHighlightFromDbThrowException) {
            Result.failure(RuntimeException("Delete highlight failed"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun insertHighlightIntoDb(highlight: Highlight): Result<Unit> {
        insertedHighlight = highlight
        return if (shouldInsertHighlightIntoDbThrowException) {
            Result.failure(RuntimeException("Insert highlight failed"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun loadHighlightFromDb(highlightId: String): Result<Highlight> {
        loadedHighlightId = highlightId
        return if (shouldLoadHighlightFromDbThrowException) {
            Result.failure(RuntimeException("Load highlight failed"))
        } else {
            Result.success(fakeHighlight)
        }
    }
}