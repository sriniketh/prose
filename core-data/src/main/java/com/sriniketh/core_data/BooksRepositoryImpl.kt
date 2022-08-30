package com.sriniketh.core_data

import com.sriniketh.core_data.transformers.asBook
import com.sriniketh.core_data.transformers.asBookEntity
import com.sriniketh.core_data.transformers.asBookSearchResult
import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.search.BookSearch
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val remoteBookDataSource: BooksRemoteDataSource,
    private val localBookDataSource: BookDao,
    private val ioDispatcher: CoroutineDispatcher
) : BooksRepository {

    override fun searchBooks(searchQuery: String): Flow<Result<BookSearch>> = flow {
        try {
            val books = remoteBookDataSource.getVolumes(searchQuery).asBookSearchResult()
            emit(Result.success(books))
        } catch (exception: Exception) {
            Timber.e(exception)
            emit(Result.failure(exception))
        }
    }.flowOn(ioDispatcher)

    override fun getBook(volumeId: String): Flow<Result<Book>> = flow {
        try {
            val book = remoteBookDataSource.getVolume(volumeId).asBook()
            emit(Result.success(book))
        } catch (exception: Exception) {
            Timber.e(exception)
            emit(Result.failure(exception))
        }
    }.flowOn(ioDispatcher)

    override fun insertBook(book: Book): Flow<Result<Unit>> = flow {
        try {
            localBookDataSource.insertBook(book.asBookEntity())
            emit(Result.success(Unit))
        } catch (exception: Exception) {
            Timber.e(exception)
            emit(Result.failure(exception))
        }
    }.flowOn(ioDispatcher)
}
