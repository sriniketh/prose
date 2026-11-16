package com.sriniketh.core_platform

import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class DateTimeSourceImpl @Inject constructor(
    private val clock: Clock
) : DateTimeSource {
    override fun now(): LocalDateTime = LocalDateTime.now(clock)
}

interface DateTimeSource {
    fun now(): LocalDateTime
}
