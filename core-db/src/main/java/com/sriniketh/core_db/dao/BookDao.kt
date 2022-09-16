package com.sriniketh.core_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_db.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM bookEntity")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Insert
    suspend fun insertHighlight(highlightEntity: HighlightEntity)

    @Query("SELECT * FROM highlightEntity WHERE bookId = :bookId")
    fun getAllHighlightsForBook(bookId: String): Flow<List<HighlightEntity>>
}
