package com.sriniketh.core_platform

import android.net.Uri

fun String?.buildHttpsUri(): Uri? =
    if (this == null) null else Uri.parse(this).buildUpon().apply { scheme("https") }.build()
