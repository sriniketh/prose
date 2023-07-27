package com.sriniketh.core_data.fakes

import com.sriniketh.core_db.dao.HighlightDao
import com.sriniketh.core_db.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeHighlightDao : HighlightDao {

    var insertedHighlightEntity: HighlightEntity? = null
    var shouldInsertHighlightThrowException = false
    override suspend fun insertHighlight(highlightEntity: HighlightEntity) {
        if (shouldInsertHighlightThrowException) {
            throw RuntimeException("some error inserting highlight")
        }
        insertedHighlightEntity = highlightEntity
    }

    val highlightsInDb = mutableListOf<HighlightEntity>()
    var shouldGetAllHighlightsForBookThrowException = false
    var bookIdPassed: String? = null
    override fun getAllHighlightsForBook(bookId: String): Flow<List<HighlightEntity>> {
        bookIdPassed = bookId
        if (shouldGetAllHighlightsForBookThrowException) {
            return flow {
                throw RuntimeException("some error fetching all highlights")
            }
        }
        return flowOf(highlightsInDb)
    }

    var deletedHighlightEntity: HighlightEntity? = null
    var shouldDeleteHighlightThrowException = false
    override suspend fun deleteHighlight(highlight: HighlightEntity) {
        if (shouldDeleteHighlightThrowException) {
            throw RuntimeException("some error deleting highlight")
        }
        deletedHighlightEntity = highlight
    }
}
