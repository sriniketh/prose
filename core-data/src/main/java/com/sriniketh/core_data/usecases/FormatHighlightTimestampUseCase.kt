package com.sriniketh.core_data.usecases

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class FormatHighlightTimestampUseCase @Inject constructor() {

    operator fun invoke(epochMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a", Locale.getDefault())
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(formatter)
    }
}
