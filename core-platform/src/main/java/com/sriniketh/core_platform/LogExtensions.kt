package com.sriniketh.core_platform

inline fun <reified T> T.logTag(): String = "PROSE_DEBUG_LOG: ${T::class.java.simpleName}"
