package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadHighlightUseCaseTest {

    private lateinit var highlightsRepository: FakeHighlightsRepository
    private lateinit var loadHighlightUseCase: LoadHighlightUseCase

    @Before
    fun setup() {
        highlightsRepository = FakeHighlightsRepository()
        loadHighlightUseCase = LoadHighlightUseCase(highlightsRepository)
    }

    @Test
    fun `when repository load succeeds then returns success result`() = runTest {
        val highlightId = "test-highlight-id"
        highlightsRepository.shouldLoadHighlightFromDbThrowException = false
        val result = loadHighlightUseCase(highlightId)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository load succeeds then highlight id is passed to repository`() = runTest {
        val highlightId = "test-highlight-id"
        highlightsRepository.shouldLoadHighlightFromDbThrowException = false
        loadHighlightUseCase(highlightId)
        assertEquals(highlightId, highlightsRepository.highlightIdPassed)
    }

    @Test
    fun `when repository load succeeds then returns highlight`() = runTest {
        val highlightId = "test-highlight-id"
        highlightsRepository.shouldLoadHighlightFromDbThrowException = false
        val result = loadHighlightUseCase(highlightId)
        val highlight = result.getOrNull()
        assertEquals("test-highlight-id", highlight?.id)
        assertEquals("test-book-id", highlight?.bookId)
        assertEquals("Test highlight text", highlight?.text)
    }

    @Test
    fun `when repository load fails then returns failure result`() = runTest {
        val highlightId = "test-highlight-id"
        highlightsRepository.shouldLoadHighlightFromDbThrowException = true
        val result = loadHighlightUseCase(highlightId)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository load fails then returns failure result with correct exception`() = runTest {
        val highlightId = "test-highlight-id"
        highlightsRepository.shouldLoadHighlightFromDbThrowException = true
        val result = loadHighlightUseCase(highlightId)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Load highlight failed", exception?.message)
    }
}
