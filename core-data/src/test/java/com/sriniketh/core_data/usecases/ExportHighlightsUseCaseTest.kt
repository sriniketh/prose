package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import com.sriniketh.core_data.fakes.FakeFileSource
import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import com.sriniketh.core_data.models.HighlightsExport
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportHighlightsUseCaseTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var useCase: ExportHighlightsUseCase

    @Before
    fun setup() {
        fakeBooksRepository = FakeBooksRepository()
        fakeHighlightsRepository = FakeHighlightsRepository()
        fakeFileSource = FakeFileSource()
        useCase = ExportHighlightsUseCase(fakeBooksRepository, fakeHighlightsRepository, fakeFileSource)
    }

    @Test
    fun `when invoked with valid bookId then returns success with uri`() = runTest {
        val result = useCase("test-book-id")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
    }

    @Test
    fun `when invoked then writes json with all book fields and highlights to cache`() = runTest {
        useCase("test-book-id")
        val writtenContent = fakeFileSource.lastWrittenContent!!
        assertTrue(writtenContent.contains("\"title\""))
        assertTrue(writtenContent.contains("Test Title"))
        assertTrue(writtenContent.contains("\"authors\""))
        assertTrue(writtenContent.contains("Test Author"))
        assertTrue(writtenContent.contains("\"publisher\""))
        assertTrue(writtenContent.contains("\"highlights\""))
        assertTrue(writtenContent.contains("Test highlight text"))
        assertEquals("test_title_export.json", fakeFileSource.lastWrittenFileName)
    }

    @Test
    fun `when book not found then returns failure`() = runTest {
        fakeBooksRepository.shouldGetBookByIdFromDbThrowException = true
        val result = useCase("nonexistent-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when highlights retrieval fails then returns failure`() = runTest {
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        val result = useCase("test-book-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when file write fails then returns failure`() = runTest {
        fakeFileSource.shouldWriteToFileFail = true
        val result = useCase("test-book-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `written json round-trips back into HighlightsExport`() = runTest {
        useCase("test-book-id")
        val writtenContent = fakeFileSource.lastWrittenContent!!
        val decoded = Json.decodeFromString<HighlightsExport>(writtenContent)
        assertEquals("Test Title", decoded.info.title)
        assertEquals("Test Author", decoded.info.authors[0])
        assertEquals("Test highlight text", decoded.highlights[0].text)
    }

    @Test
    fun `written json omits null optional book fields rather than emitting nulls`() = runTest {
        fakeBooksRepository.fakeBookToReturn = Book(
            id = "test-id",
            info = BookInfo(
                title = "Test Title",
                subtitle = null,
                authors = listOf("Test Author"),
                thumbnailLink = null,
                publisher = null,
                publishedDate = null,
                description = null,
                pageCount = null,
                averageRating = null,
                ratingsCount = null
            )
        )
        useCase("test-book-id")
        val writtenContent = fakeFileSource.lastWrittenContent!!
        assertFalse(writtenContent.contains("null"))
        assertFalse(writtenContent.contains("\"publisher\""))
        assertFalse(writtenContent.contains("\"subtitle\""))
    }
}
