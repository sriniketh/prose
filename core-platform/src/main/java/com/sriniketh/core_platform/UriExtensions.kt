package com.sriniketh.core_platform

import android.net.Uri
import androidx.core.net.toUri

fun String?.buildHttpsUri(): Uri? =
    if (this == null) null else Uri.parse(this).buildUpon().apply { scheme("https") }.build()

fun Uri.encodeUri(): String = Uri.encode(this.toString())

fun String.decodeUri(): Uri = Uri.decode(this).toUri()
