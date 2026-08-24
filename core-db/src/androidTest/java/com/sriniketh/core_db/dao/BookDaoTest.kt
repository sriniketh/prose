package com.sriniketh.core_db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_db.BookDatabase
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var database: BookDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookDatabase::class.java
        ).build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createBook(
        id: String = "book-1",
        title: String = "Dune"
    ) = BookEntity(
        id = id,
        title = title,
        subtitle = "subtitle",
        authors = listOf("Frank Herbert"),
        thumbnailLink = "https://example.com/thumbnail.png",
        publisher = "Chilton Books",
        publishedDate = "1965",
        description = "A science fiction novel",
        pageCount = 412,
        averageRating = 4.5,
        ratingsCount = 100
    )

    @Test
    fun insertBookThenGetAllBooksEmitsInsertedBook() = runBlocking {
        val book = createBook()

        bookDao.insertBook(book)

        val books = bookDao.getAllBooks().first()
        assertEquals(listOf(book), books)
    }

    @Test
    fun insertBookWithDuplicateIdIsIgnored() = runBlocking {
        val original = createBook(title = "Original Title")
        val duplicate = createBook(title = "Duplicate Title")

        bookDao.insertBook(original)
        bookDao.insertBook(duplicate)

        val books = bookDao.getAllBooks().first()
        assertEquals(listOf(original), books)
    }

    @Test
    fun doesBookExistReturnsTrueWhenBookIsInserted() = runBlocking {
        val book = createBook()
        bookDao.insertBook(book)

        assertTrue(bookDao.doesBookExist(book.id))
    }

    @Test
    fun doesBookExistReturnsFalseWhenBookIsNotInserted() = runBlocking {
        assertFalse(bookDao.doesBookExist("missing-book"))
    }

    @Test
    fun getBookByIdReturnsBookWhenPresent() = runBlocking {
        val book = createBook()
        bookDao.insertBook(book)

        assertEquals(book, bookDao.getBookById(book.id))
    }

    @Test
    fun getBookByIdReturnsNullWhenAbsent() = runBlocking {
        assertNull(bookDao.getBookById("missing-book"))
    }

    @Test
    fun deleteBookRemovesBookFromGetAllBooks() = runBlocking {
        val book = createBook()
        bookDao.insertBook(book)

        bookDao.deleteBook(book)

        val books = bookDao.getAllBooks().first()
        assertTrue(books.isEmpty())
    }
}
