package com.sriniketh.core_db.dagger

import android.content.Context
import androidx.room.Room
import com.sriniketh.core_db.BookDatabase
import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_db.dao.HighlightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesBookDatabase(
        @ApplicationContext context: Context
    ): BookDatabase {
        return Room.databaseBuilder(context, BookDatabase::class.java, "book-db").build()
    }

    @Provides
    fun providesBookDao(
        database: BookDatabase
    ): BookDao = database.bookDao()

    @Provides
    fun providesHighlightDao(
        database: BookDatabase
    ): HighlightDao = database.highlightDao()
}
