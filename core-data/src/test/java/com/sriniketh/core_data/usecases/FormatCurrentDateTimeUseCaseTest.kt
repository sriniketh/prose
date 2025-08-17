package com.sriniketh.core_data.usecases

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class FormatCurrentDateTimeUseCaseTest {

    private lateinit var formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase

    @Before
    fun setup() {
        formatCurrentDateTimeUseCase = FormatCurrentDateTimeUseCase()
    }

    @Test
    fun `when formatting morning time then returns correct AM format`() {
        val dateTime = LocalDateTime.of(2023, 12, 25, 9, 30)
        val result = formatCurrentDateTimeUseCase(dateTime)
        assertEquals("12-25-2023 09:30 AM", result)
    }

    @Test
    fun `when formatting afternoon time then returns correct PM format`() {
        val dateTime = LocalDateTime.of(2023, 12, 25, 15, 45)
        val result = formatCurrentDateTimeUseCase(dateTime)
        assertEquals("12-25-2023 03:45 PM", result)
    }

    @Test
    fun `when formatting midnight then returns correct AM format`() {
        val dateTime = LocalDateTime.of(2023, 1, 1, 0, 0)
        val result = formatCurrentDateTimeUseCase(dateTime)
        assertEquals("01-01-2023 12:00 AM", result)
    }

    @Test
    fun `when formatting noon then returns correct PM format`() {
        val dateTime = LocalDateTime.of(2023, 6, 15, 12, 0)
        val result = formatCurrentDateTimeUseCase(dateTime)
        assertEquals("06-15-2023 12:00 PM", result)
    }

    @Test
    fun `when formatting single digit month and day then pads with zeros`() {
        val dateTime = LocalDateTime.of(2023, 3, 5, 8, 7)
        val result = formatCurrentDateTimeUseCase(dateTime)
        assertEquals("03-05-2023 08:07 AM", result)
    }
}
