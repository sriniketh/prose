package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.HighlightEntity
import com.sriniketh.core_models.book.Highlight

fun HighlightEntity.asHighlight() = Highlight(id, bookId, text, savedOnTimestamp)
