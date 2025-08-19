package com.sriniketh.feature_addhighlight.fakes

import com.sriniketh.core_platform.DateTimeSource
import java.time.LocalDateTime

class FakeDateTimeSource : DateTimeSource {

    var currentTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

    override fun now(): LocalDateTime = currentTime
}
