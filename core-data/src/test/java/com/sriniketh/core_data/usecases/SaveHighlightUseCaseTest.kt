package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveHighlightUseCaseTest {

    private lateinit var highlightsRepository: FakeHighlightsRepository
    private lateinit var saveHighlightUseCase: SaveHighlightUseCase

    @Before
    fun setup() {
        highlightsRepository = FakeHighlightsRepository()
        saveHighlightUseCase = SaveHighlightUseCase(highlightsRepository)
    }

    @Test
    fun `when repository insertion succeeds then returns success result`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldInsertHighlightIntoDbThrowException = false
        val result = saveHighlightUseCase(highlight)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository insertion succeeds then highlight is passed to repository`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldInsertHighlightIntoDbThrowException = false
        saveHighlightUseCase(highlight)
        assertEquals(highlight, highlightsRepository.insertedHighlight)
    }

    @Test
    fun `when repository insertion fails then returns failure result`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldInsertHighlightIntoDbThrowException = true
        val result = saveHighlightUseCase(highlight)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository insertion fails then returns failure result with correct exception`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldInsertHighlightIntoDbThrowException = true
        val result = saveHighlightUseCase(highlight)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Insert highlight failed", exception?.message)
    }

    private fun createTestHighlight() = Highlight(
        id = "test-highlight-id",
        bookId = "test-book-id",
        text = "Test highlight text",
        savedOnTimestamp = "2023-01-01 12:00 PM"
    )
}
