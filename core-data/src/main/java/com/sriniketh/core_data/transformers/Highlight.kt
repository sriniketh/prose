package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.HighlightEntity
import com.sriniketh.core_models.book.Highlight

fun Highlight.asHighlightEntity() = HighlightEntity(id, bookId, text, savedOnTimestamp)
