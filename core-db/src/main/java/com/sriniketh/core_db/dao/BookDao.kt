package com.sriniketh.core_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM bookEntity")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM BookEntity WHERE id = :bookId")
    fun doesBookExist(bookId: String): Boolean
}
