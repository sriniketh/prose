package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import javax.inject.Inject

class CreateFileUseCase @Inject constructor(
    private val fileSource: FileSource
) {
    operator fun invoke(): Uri = fileSource.createNewFile()
}
