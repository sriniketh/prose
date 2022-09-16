package com.sriniketh.core_db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = CASCADE
    )],
    indices = [Index("bookId")]
)
data class HighlightEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
