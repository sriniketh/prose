package com.sriniketh.core_data

import com.sriniketh.core_data.transformers.asBook
import com.sriniketh.core_data.transformers.asBookEntity
import com.sriniketh.core_data.transformers.asBookSearchResult
import com.sriniketh.core_data.transformers.asHighlight
import com.sriniketh.core_data.transformers.asHighlightEntity
import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_models.search.BookSearch
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val remoteBookDataSource: BooksRemoteDataSource,
    private val localBookDataSource: BookDao,
    private val ioDispatcher: CoroutineDispatcher
) : BooksRepository {

    override fun searchForBooks(searchQuery: String): Flow<Result<BookSearch>> =
        flow {
            val books = remoteBookDataSource.getVolumes(searchQuery).asBookSearchResult()
            emit(Result.success(books))
        }.catch { exception ->
            Timber.e(exception)
            emit(Result.failure(exception))
        }.flowOn(ioDispatcher)

    override fun fetchBookInfo(volumeId: String): Flow<Result<Book>> =
        flow {
            val book = remoteBookDataSource.getVolume(volumeId).asBook()
            emit(Result.success(book))
        }.catch { exception ->
            Timber.e(exception)
            emit(Result.failure(exception))
        }.flowOn(ioDispatcher)

    override fun insertBookIntoDb(book: Book): Flow<Result<Unit>> =
        flow {
            localBookDataSource.insertBook(book.asBookEntity())
            emit(Result.success(Unit))
        }.catch { exception ->
            Timber.e(exception)
            emit(Result.failure(exception))
        }.flowOn(ioDispatcher)

    override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> =
        localBookDataSource.getAllBooks()
            .map { entities ->
                Result.success(entities.map { entity -> entity.asBook() })
            }
            .catch { exception ->
                Timber.e(exception)
                emit(Result.failure(exception))
            }.flowOn(ioDispatcher)

    override fun insertHighlightIntoDb(highlight: Highlight): Flow<Result<Unit>> =
        flow {
            localBookDataSource.insertHighlight(highlight.asHighlightEntity())
            emit(Result.success(Unit))
        }.catch { exception ->
            Timber.e(exception)
            emit(Result.failure(exception))
        }.flowOn(ioDispatcher)

    override fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>> =
        localBookDataSource.getAllHighlightsForBook(bookId)
            .map { entities ->
                Result.success(entities.map { entity -> entity.asHighlight() })
            }
            .catch { exception ->
                Timber.e(exception)
                emit(Result.failure(exception))
            }.flowOn(ioDispatcher)
}
