package com.sriniketh.core_data.usecases

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class FormatCurrentDateTimeUseCase @Inject constructor() {

    private val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a")

    operator fun invoke(dateTime: LocalDateTime): String = dateTime.format(formatter)
}
