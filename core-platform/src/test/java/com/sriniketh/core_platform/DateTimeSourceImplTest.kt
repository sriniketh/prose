package com.sriniketh.core_platform

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeSourceImplTest {

    @Test
    fun `now returns the current time from the injected clock`() {
        val zone = ZoneId.of("UTC")
        val fixedInstant = Instant.parse("2024-03-15T10:30:00Z")
        val clock = Clock.fixed(fixedInstant, zone)
        val dateTimeSource = DateTimeSourceImpl(clock)

        val result = dateTimeSource.now()

        assertEquals(LocalDateTime.ofInstant(fixedInstant, zone), result)
    }

    @Test
    fun `now reflects a different clock's time and zone`() {
        val zone = ZoneId.of("America/New_York")
        val fixedInstant = Instant.parse("2024-03-15T10:30:00Z")
        val clock = Clock.fixed(fixedInstant, zone)
        val dateTimeSource = DateTimeSourceImpl(clock)

        val result = dateTimeSource.now()

        assertEquals(LocalDateTime.ofInstant(fixedInstant, zone), result)
    }
}
