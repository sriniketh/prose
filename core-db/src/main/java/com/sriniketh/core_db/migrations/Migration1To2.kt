package com.sriniketh.core_db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val MIGRATION_1_2: Migration = object : Migration(1, 2) {

    private val legacyTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a", Locale.US)

    override fun migrate(db: SupportSQLiteDatabase) {
        migratePipeDelimitedAuthorsToJson(db)
        migrateFormattedTimestampToEpochMillis(db)
    }

    private fun migratePipeDelimitedAuthorsToJson(db: SupportSQLiteDatabase) {
        val idToAuthors = mutableMapOf<String, List<String>>()
        db.query("SELECT id, authors FROM BookEntity").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val authorsIndex = cursor.getColumnIndexOrThrow("authors")
            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex)
                val rawAuthors = cursor.getString(authorsIndex)
                idToAuthors[id] = if (rawAuthors.isEmpty()) emptyList() else rawAuthors.split("|")
            }
        }

        idToAuthors.forEach { (id, authors) ->
            db.execSQL(
                "UPDATE BookEntity SET authors = ? WHERE id = ?",
                arrayOf(Json.encodeToString(authors), id)
            )
        }
    }

    private fun migrateFormattedTimestampToEpochMillis(db: SupportSQLiteDatabase) {
        data class LegacyHighlightRow(
            val id: String,
            val bookId: String,
            val text: String,
            val savedOnEpochMillis: Long
        )

        val rows = mutableListOf<LegacyHighlightRow>()
        db.query("SELECT id, bookId, text, savedOnTimestamp FROM HighlightEntity").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val bookIdIndex = cursor.getColumnIndexOrThrow("bookId")
            val textIndex = cursor.getColumnIndexOrThrow("text")
            val timestampIndex = cursor.getColumnIndexOrThrow("savedOnTimestamp")
            while (cursor.moveToNext()) {
                rows.add(
                    LegacyHighlightRow(
                        id = cursor.getString(idIndex),
                        bookId = cursor.getString(bookIdIndex),
                        text = cursor.getString(textIndex),
                        savedOnEpochMillis = parseLegacyTimestamp(cursor.getString(timestampIndex))
                    )
                )
            }
        }

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `HighlightEntity_new` (`id` TEXT NOT NULL, `bookId` TEXT NOT NULL, " +
                "`text` TEXT NOT NULL, `savedOnEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`bookId`) REFERENCES `BookEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )

        rows.forEach { row ->
            db.execSQL(
                "INSERT INTO `HighlightEntity_new` (id, bookId, text, savedOnEpochMillis) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(row.id, row.bookId, row.text, row.savedOnEpochMillis)
            )
        }

        db.execSQL("DROP TABLE `HighlightEntity`")
        db.execSQL("ALTER TABLE `HighlightEntity_new` RENAME TO `HighlightEntity`")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_HighlightEntity_bookId` ON `HighlightEntity` (`bookId`)"
        )
    }

    private fun parseLegacyTimestamp(value: String): Long = try {
        LocalDateTime.parse(value, legacyTimestampFormatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: Exception) {
        0L
    }
}
