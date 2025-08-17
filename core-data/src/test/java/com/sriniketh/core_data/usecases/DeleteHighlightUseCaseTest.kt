package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteHighlightUseCaseTest {

    private lateinit var highlightsRepository: FakeHighlightsRepository
    private lateinit var deleteHighlightUseCase: DeleteHighlightUseCase

    @Before
    fun setup() {
        highlightsRepository = FakeHighlightsRepository()
        deleteHighlightUseCase = DeleteHighlightUseCase(highlightsRepository)
    }

    @Test
    fun `when repository deletion succeeds then returns success result`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = false
        val result = deleteHighlightUseCase(highlight)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository deletion succeeds then highlight is passed to repository`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = false
        deleteHighlightUseCase(highlight)
        assertEquals(highlight, highlightsRepository.deletedHighlight)
    }

    @Test
    fun `when repository deletion fails then returns failure result`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = true
        val result = deleteHighlightUseCase(highlight)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository deletion fails then returns failure result with correct exception`() = runTest {
        val highlight = createTestHighlight()
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = true
        val result = deleteHighlightUseCase(highlight)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Delete highlight failed", exception?.message)
    }

    private fun createTestHighlight() = Highlight(
        id = "test-highlight-id",
        bookId = "test-book-id",
        text = "Test highlight text",
        savedOnTimestamp = "2023-01-01 12:00 PM"
    )
}
