package com.sriniketh.feature_addhighlight

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.sriniketh.core_platform.dagger.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetRotatedBitmapUseCase @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(imageUri: Uri): Bitmap? = withContext(ioDispatcher) {
        ImageRotater.getRotatedBitmap(appContext, imageUri)
    }
}
