package com.sriniketh.core_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertBook(book: BookEntity)

	@Query("SELECT * FROM bookEntity")
	fun getAllBooks(): Flow<List<BookEntity>>

	@Query("SELECT COUNT(*) FROM BookEntity WHERE id = :bookId")
	suspend fun doesBookExist(bookId: String): Boolean

	@Delete
	suspend fun deleteBook(book: BookEntity)
}
