package com.sriniketh.core_data.usecases

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class FormatHighlightTimestampUseCaseTest {

    private lateinit var formatHighlightTimestampUseCase: FormatHighlightTimestampUseCase
    private lateinit var originalLocale: Locale

    @Before
    fun setup() {
        formatHighlightTimestampUseCase = FormatHighlightTimestampUseCase()
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    private fun epochMillisOf(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test
    fun `when formatting morning time then returns correct AM format`() {
        val epochMillis = epochMillisOf(LocalDateTime.of(2023, 12, 25, 9, 30))
        val result = formatHighlightTimestampUseCase(epochMillis)
        assertEquals("12-25-2023 09:30 AM", result)
    }

    @Test
    fun `when formatting afternoon time then returns correct PM format`() {
        val epochMillis = epochMillisOf(LocalDateTime.of(2023, 12, 25, 15, 45))
        val result = formatHighlightTimestampUseCase(epochMillis)
        assertEquals("12-25-2023 03:45 PM", result)
    }

    @Test
    fun `when formatting midnight then returns correct AM format`() {
        val epochMillis = epochMillisOf(LocalDateTime.of(2023, 1, 1, 0, 0))
        val result = formatHighlightTimestampUseCase(epochMillis)
        assertEquals("01-01-2023 12:00 AM", result)
    }

    @Test
    fun `when formatting noon then returns correct PM format`() {
        val epochMillis = epochMillisOf(LocalDateTime.of(2023, 6, 15, 12, 0))
        val result = formatHighlightTimestampUseCase(epochMillis)
        assertEquals("06-15-2023 12:00 PM", result)
    }

    @Test
    fun `when formatting single digit month and day then pads with zeros`() {
        val epochMillis = epochMillisOf(LocalDateTime.of(2023, 3, 5, 8, 7))
        val result = formatHighlightTimestampUseCase(epochMillis)
        assertEquals("03-05-2023 08:07 AM", result)
    }
}
