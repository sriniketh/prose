package com.sriniketh.core_platform

import java.time.LocalDateTime
import javax.inject.Inject

class DateTimeSourceImpl @Inject constructor() : DateTimeSource {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

interface DateTimeSource {
    fun now(): LocalDateTime
}
