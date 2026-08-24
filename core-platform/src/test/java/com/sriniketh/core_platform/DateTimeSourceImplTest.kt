package com.sriniketh.core_platform

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertFalse
import org.junit.Test

class DateTimeSourceImplTest {

    @Test
    fun `now returns a value within one second of the real clock`() {
        val dateTimeSource = DateTimeSourceImpl()

        val before = LocalDateTime.now()
        val result = dateTimeSource.now()
        val after = LocalDateTime.now()

        assertFalse(result.isBefore(before.minus(1, ChronoUnit.SECONDS)))
        assertFalse(result.isAfter(after.plus(1, ChronoUnit.SECONDS)))
    }
}
