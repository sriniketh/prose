package com.sriniketh.core_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sriniketh.core_db.converters.ListTypeConverter
import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_db.entity.HighlightEntity

@Database(entities = [BookEntity::class, HighlightEntity::class], version = 1, exportSchema = false)
@TypeConverters(ListTypeConverter::class)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
}
