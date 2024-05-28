package com.sriniketh.core_platform

import android.net.Uri

interface FileSource {
    fun createNewFile(): Uri
    fun deleteFile(uri: Uri): Boolean
}
