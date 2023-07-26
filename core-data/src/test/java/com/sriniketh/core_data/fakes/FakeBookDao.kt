package com.sriniketh.core_data.fakes

import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeBookDao : BookDao {

    var insertedBookEntity: BookEntity? = null
    var shouldInsertBookThrowException = false
    override suspend fun insertBook(book: BookEntity) {
        if (shouldInsertBookThrowException) {
            throw RuntimeException("some error inserting book")
        }
        insertedBookEntity = book
    }

    val booksInDb = mutableListOf<BookEntity>()
    var shouldGetAllBooksThrowException = false
    override fun getAllBooks(): Flow<List<BookEntity>> {
        if (shouldGetAllBooksThrowException) {
            return flow {
                throw RuntimeException("some error fetching all books")
            }
        }
        return flowOf(booksInDb)
    }
}
