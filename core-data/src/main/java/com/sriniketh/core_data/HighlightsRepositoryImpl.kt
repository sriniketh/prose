package com.sriniketh.core_data

import com.sriniketh.core_data.transformers.asHighlight
import com.sriniketh.core_data.transformers.asHighlightEntity
import com.sriniketh.core_db.dao.HighlightDao
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class HighlightsRepositoryImpl @Inject constructor(
    private val localHighlightsDataSource: HighlightDao,
    private val ioDispatcher: CoroutineDispatcher
) : HighlightsRepository {

    override suspend fun insertHighlightIntoDb(highlight: Highlight): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                localHighlightsDataSource.insertHighlight(highlight.asHighlightEntity())
                Result.success(Unit)
            } catch (exception: Exception) {
                Timber.e(exception)
                Result.failure(exception)
            }
        }

    override suspend fun loadHighlightFromDb(highlightId: String): Result<Highlight> =
        withContext(ioDispatcher) {
            try {
                val highlight =
                    localHighlightsDataSource.getHighlightById(highlightId).asHighlight()
                Result.success(highlight)
            } catch (exception: Exception) {
                Timber.e(exception)
                Result.failure(exception)
            }
        }

    override fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>> =
        localHighlightsDataSource.getAllHighlightsForBook(bookId)
            .map { entities ->
                Result.success(entities.map { entity -> entity.asHighlight() })
            }
            .catch { exception ->
                Timber.e(exception)
                emit(Result.failure(exception))
            }.flowOn(ioDispatcher)

    override suspend fun deleteHighlightFromDb(highlight: Highlight): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                localHighlightsDataSource.deleteHighlight(highlight.asHighlightEntity())
                Result.success(Unit)
            } catch (exception: Exception) {
                Timber.e(exception)
                Result.failure(exception)
            }
        }
}
