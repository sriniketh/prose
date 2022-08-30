package com.sriniketh.core_db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.sriniketh.core_db.entity.BookEntity

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: BookEntity)
}
