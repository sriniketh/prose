package com.sriniketh.core_data

import app.cash.turbine.test
import com.sriniketh.core_data.fakes.FakeHighlightDao
import com.sriniketh.core_db.entity.HighlightEntity
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HighlightsRepositoryImplTest {

    private lateinit var highlightDao: FakeHighlightDao
    private lateinit var highlightsRepositoryImpl: HighlightsRepositoryImpl

    @Before
    fun setup() {
        highlightDao = FakeHighlightDao()
        highlightsRepositoryImpl = HighlightsRepositoryImpl(highlightDao)
    }

    @Test
    fun `insertHighlightIntoDb returns success result when highlight is inserted into db`() =
        runTest {
            val highlight = Highlight(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )
            val highlightEntity = HighlightEntity(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )

            highlightDao.shouldInsertHighlightThrowException = false
            val result = highlightsRepositoryImpl.insertHighlightIntoDb(highlight)
            assertTrue(result.isSuccess)
            assertEquals(Unit, result.getOrNull())
            assertEquals(highlightEntity, highlightDao.insertedHighlightEntity)
        }

    @Test
    fun `insertHighlightIntoDb returns failure result when exception occurs during insertion`() =
        runTest {
            val highlight = Highlight(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )

            highlightDao.shouldInsertHighlightThrowException = true
            val result = highlightsRepositoryImpl.insertHighlightIntoDb(highlight)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error inserting highlight", exception?.message)
        }

    @Test
    fun `loadHighlightFromDb returns success result when highlight present in db`() =
        runTest {
            val highlightEntity = HighlightEntity(
                id = "someid", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )
            highlightDao.highlightEntityToReturn = highlightEntity
            highlightDao.shouldGetHighlightByIdThrowException = false

            val result = highlightsRepositoryImpl.loadHighlightFromDb("someid")
            assertTrue(result.isSuccess)
            val highlight = result.getOrNull()
            assertEquals("someid", highlight?.id)
            assertEquals("eius", highlight?.bookId)
            assertEquals("ignota", highlight?.text)
            assertEquals("diam", highlight?.savedOnTimestamp)
        }

    @Test
    fun `loadHighlightFromDb returns failure result when exception occurs during retrieval`() =
        runTest {
            highlightDao.shouldGetHighlightByIdThrowException = true

            val result = highlightsRepositoryImpl.loadHighlightFromDb("someid")
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error fetching highlight by id", exception?.message)
        }

    @Test
    fun `getAllHighlightsForBookFromDb returns flow with success result when highlights are retrieved from db`() =
        runTest {
            highlightDao.shouldGetAllHighlightsForBookThrowException = false
            highlightDao.highlightsInDb.add(
                HighlightEntity(
                    id = "iaculis", bookId = "pellentesque", text = "nisi", savedOnTimestamp = "vim"
                )
            )
            highlightsRepositoryImpl.getAllHighlightsForBookFromDb("someId").test {
                val result = awaitItem()
                assertTrue(result.isSuccess)
                val highlights = result.getOrNull()
                assertEquals(1, highlights?.size)
                val highlight = highlights?.first()
                assertEquals("iaculis", highlight?.id)
                assertEquals("pellentesque", highlight?.bookId)
                assertEquals("nisi", highlight?.text)
                assertEquals("vim", highlight?.savedOnTimestamp)
                awaitComplete()
            }
            assertEquals("someId", highlightDao.bookIdPassed)
        }

    @Test
    fun `getAllHighlightsForBookFromDb returns flow with failure result when exception occurs during retrieval`() =
        runTest {
            highlightDao.shouldGetAllHighlightsForBookThrowException = true
            highlightDao.highlightsInDb.add(
                HighlightEntity(
                    id = "iaculis", bookId = "pellentesque", text = "nisi", savedOnTimestamp = "vim"
                )
            )

            highlightsRepositoryImpl.getAllHighlightsForBookFromDb("someId").test {
                val result = awaitItem()
                assertTrue(result.isFailure)
                val exception = result.exceptionOrNull()
                assertTrue(exception is RuntimeException)
                assertEquals("some error fetching all highlights", exception?.message)
                awaitComplete()
            }
            assertEquals("someId", highlightDao.bookIdPassed)
        }

    @Test
    fun `deleteHighlightFromDb returns success result when highlight is deleted from db`() =
        runTest {
            val highlight = Highlight(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )
            val highlightEntity = HighlightEntity(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )

            highlightDao.shouldDeleteHighlightThrowException = false
            val result = highlightsRepositoryImpl.deleteHighlightFromDb(highlight)
            assertTrue(result.isSuccess)
            assertEquals(Unit, result.getOrNull())
            assertEquals(highlightEntity, highlightDao.deletedHighlightEntity)
        }

    @Test
    fun `deleteHighlightFromDb returns failure result when exception occurs during deletion`() =
        runTest {
            val highlight = Highlight(
                id = "possim", bookId = "eius", text = "ignota", savedOnTimestamp = "diam"
            )

            highlightDao.shouldDeleteHighlightThrowException = true
            val result = highlightsRepositoryImpl.deleteHighlightFromDb(highlight)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("some error deleting highlight", exception?.message)
        }
}
