package com.sriniketh.core_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sriniketh.core_db.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    @Insert
    suspend fun insertHighlight(highlightEntity: HighlightEntity)

    @Query("SELECT * FROM highlightEntity WHERE bookId = :bookId")
    fun getAllHighlightsForBook(bookId: String): Flow<List<HighlightEntity>>

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)
}
