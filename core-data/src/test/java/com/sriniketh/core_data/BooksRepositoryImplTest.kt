package com.sriniketh.core_data

import app.cash.turbine.test
import com.sriniketh.core_data.fakes.FakeBookDao
import com.sriniketh.core_data.fakes.FakeBooksRemoteDataSource
import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BooksRepositoryImplTest {

    private lateinit var booksRemoteDataSource: FakeBooksRemoteDataSource
    private lateinit var bookDao: FakeBookDao
    private lateinit var dispatcher: CoroutineDispatcher
    private lateinit var booksRepositoryImpl: BooksRepositoryImpl

    @Before
    fun setup() {
        booksRemoteDataSource = FakeBooksRemoteDataSource()
        bookDao = FakeBookDao()
        dispatcher = Dispatchers.Unconfined
        booksRepositoryImpl = BooksRepositoryImpl(booksRemoteDataSource, bookDao, dispatcher)
    }

    @Test
    fun `searchForBooks returns success Result when able to successfully retrieve books from remote`() =
        runTest {
            booksRemoteDataSource.shouldGetVolumesThrowException = false
            assertNull(booksRemoteDataSource.searchQueryPassed)
            val result = booksRepositoryImpl.searchForBooks("some query")
            assertEquals("some query", booksRemoteDataSource.searchQueryPassed)
            assertTrue(result.isSuccess)
            val items = result.getOrNull()!!.items
            assertEquals(1, items.size)

            val book = items[0]
            assertEquals("someId1", book.id)
            val bookInfo = book.info
            assertEquals("title 1", bookInfo.title)
            assertEquals("subtitle 1", bookInfo.subtitle)
            assertEquals("description 1", bookInfo.description)
            assertEquals("author 1", bookInfo.authors[0])
            assertEquals("author 2", bookInfo.authors[1])
            assertEquals("thumbnail 1", bookInfo.thumbnailLink)
            assertEquals("publisher 1", bookInfo.publisher)
            assertEquals("published date 1", bookInfo.publishedDate)
            assertEquals(100, bookInfo.pageCount)
            assertEquals(4.5, bookInfo.averageRating)
            assertEquals(4500, bookInfo.ratingsCount)
        }

    @Test
    fun `searchForBooks returns failure Result when something goes wrong during retrieval`() =
        runTest {
            booksRemoteDataSource.shouldGetVolumesThrowException = true
            assertNull(booksRemoteDataSource.searchQueryPassed)
            val result = booksRepositoryImpl.searchForBooks("some query")
            assertEquals("some query", booksRemoteDataSource.searchQueryPassed)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error fetching volumes", exception?.message)
        }

    @Test
    fun `fetchBookInfo returns success Result when able to successfully retrive book info`() =
        runTest {
            booksRemoteDataSource.shouldGetVolumeThrowException = false
            assertNull(booksRemoteDataSource.volumeIdPassed)
            val result = booksRepositoryImpl.fetchBookInfo("someId")
            assertEquals("someId", booksRemoteDataSource.volumeIdPassed)
            assertTrue(result.isSuccess)

            val book = result.getOrNull()!!
            assertEquals("someId1", book.id)
            val bookInfo = book.info
            assertEquals("title 1", bookInfo.title)
            assertEquals("subtitle 1", bookInfo.subtitle)
            assertEquals("description 1", bookInfo.description)
            assertEquals("author 1", bookInfo.authors[0])
            assertEquals("author 2", bookInfo.authors[1])
            assertEquals("thumbnail 1", bookInfo.thumbnailLink)
            assertEquals("publisher 1", bookInfo.publisher)
            assertEquals("published date 1", bookInfo.publishedDate)
            assertEquals(100, bookInfo.pageCount)
            assertEquals(4.5, bookInfo.averageRating)
            assertEquals(4500, bookInfo.ratingsCount)
        }

    @Test
    fun `fetchBookInfo returns failure Result when something goes wrong during retrieval`() =
        runTest {
            booksRemoteDataSource.shouldGetVolumeThrowException = true
            assertNull(booksRemoteDataSource.volumeIdPassed)
            val result = booksRepositoryImpl.fetchBookInfo("someId")
            assertEquals("someId", booksRemoteDataSource.volumeIdPassed)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error fetching volume", exception?.message)
        }

    @Test
    fun `insertBookIntoDb returns success Result when book is inserted into db`() = runTest {
        val book = Book(
            id = "someId", info = BookInfo(
                title = "some title",
                subtitle = null,
                authors = listOf(),
                thumbnailLink = null,
                publisher = null,
                publishedDate = null,
                description = null,
                pageCount = null,
                averageRating = null,
                ratingsCount = null
            )
        )
        val bookEntity = fakeBookEntity("someId")

        bookDao.shouldInsertBookThrowException = false
        val result = booksRepositoryImpl.insertBookIntoDb(book)
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        assertEquals(bookEntity, bookDao.insertedBookEntity)
    }

    @Test
    fun `insertBookIntoDb returns failure Result when something goes wrong during insertion`() =
        runTest {
            val book = Book(
                id = "someId", info = BookInfo(
                    title = "some title",
                    subtitle = null,
                    authors = listOf(),
                    thumbnailLink = null,
                    publisher = null,
                    publishedDate = null,
                    description = null,
                    pageCount = null,
                    averageRating = null,
                    ratingsCount = null
                )
            )
            bookDao.shouldInsertBookThrowException = true
            val result = booksRepositoryImpl.insertBookIntoDb(book)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error inserting book", exception?.message)
        }

    @Test
    fun `doesBookExist returns true when book exists in database`() = runTest {
        bookDao.booksInDb.add(fakeBookEntity("someId1"))
        val result = booksRepositoryImpl.doesBookExistInDb("someId1")
        assertTrue(result)
    }

    @Test
    fun `doesBookExist returns false when book does not exist in database`() = runTest {
        bookDao.booksInDb.add(fakeBookEntity("someId1"))
        val result = booksRepositoryImpl.doesBookExistInDb("someId2")
        assertFalse(result)
    }

    @Test
    fun `doesBookExist returns false when an exception occurs downstream`() = runTest {
        bookDao.booksInDb.add(fakeBookEntity("someId1"))
        bookDao.shouldDoesBookExistThrowException = true
        val result = booksRepositoryImpl.doesBookExistInDb("someId1")
        assertFalse(result)
    }

    @Test
    fun `getAllSavedBooksFromDb returns flow with success result when books are successfully retrieved from db`() =
        runTest {
            bookDao.shouldGetAllBooksThrowException = false
            bookDao.booksInDb.add(
                fakeBookEntity("someId")
            )
            booksRepositoryImpl.getAllSavedBooksFromDb().test {
                val result = awaitItem()
                assertTrue(result.isSuccess)
                val books = result.getOrNull()
                assertEquals(1, books?.size)
                val book = books?.first()
                assertEquals("someId", book?.id)
                assertEquals("some title", book?.info?.title)
                awaitComplete()
            }
        }

    @Test
    fun `getAllSavedBooksFromDb returns flow with failure result when something goes wrong during retrieval`() =
        runTest {
            bookDao.shouldGetAllBooksThrowException = true
            bookDao.booksInDb.add(fakeBookEntity("someId"))
            booksRepositoryImpl.getAllSavedBooksFromDb().test {
                val result = awaitItem()
                assertTrue(result.isFailure)
                val exception = result.exceptionOrNull()
                assertTrue(exception is RuntimeException)
                assertEquals("some error fetching all books", exception?.message)
                awaitComplete()
            }
        }

    private fun fakeBookEntity(bookId: String) = BookEntity(
        id = bookId,
        title = "some title",
        subtitle = null,
        authors = listOf(),
        thumbnailLink = null,
        publisher = null,
        publishedDate = null,
        description = null,
        pageCount = null,
        averageRating = null,
        ratingsCount = null
    )
}
