package com.sriniketh.core_models.book

data class Highlight(
    val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
