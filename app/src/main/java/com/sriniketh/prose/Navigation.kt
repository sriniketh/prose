package com.sriniketh.prose

import kotlinx.serialization.Serializable

@Serializable
internal data object Bookshelf

@Serializable
internal data object Search

@Serializable
internal data class BookInfo(val bookId: String)

@Serializable
internal data class ViewHighlights(val bookId: String)

@Serializable
internal data class CaptureAndCropImage(val bookId: String)

@Serializable
internal data class SaveHighlightFromUri(val bookId: String, val uri: String)

@Serializable
internal data class SaveHighlightFromHighlightId(val bookId: String, val highlightId: String)
