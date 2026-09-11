package com.sriniketh.core_db.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sriniketh.core_db.BookDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val testDbName = "migration-1-2-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BookDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesExistingBookAndHighlightRowsIntact() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-1', 'Dune', 'subtitle', 'Frank Herbert', 'https://example.com/thumb.png', " +
                    "'Chilton Books', '1965', 'A science fiction novel', 412, 4.5, 100)"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-1', 'book-1', 'Fear is the mind-killer.', '08-23-2026 12:00 PM')"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query(
            "SELECT title, subtitle, authors, thumbnailLink, publisher, publishedDate, " +
                "description, pageCount, averageRating, ratingsCount FROM BookEntity WHERE id = 'book-1'"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Dune", cursor.getString(0))
            assertEquals("subtitle", cursor.getString(1))
            assertEquals("""["Frank Herbert"]""", cursor.getString(2))
            assertEquals("https://example.com/thumb.png", cursor.getString(3))
            assertEquals("Chilton Books", cursor.getString(4))
            assertEquals("1965", cursor.getString(5))
            assertEquals("A science fiction novel", cursor.getString(6))
            assertEquals(412, cursor.getInt(7))
            assertEquals(4.5, cursor.getDouble(8), 0.0)
            assertEquals(100, cursor.getInt(9))
        }

        migratedDb.query(
            "SELECT id, bookId, text, savedOnEpochMillis FROM HighlightEntity WHERE id = 'highlight-1'"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("highlight-1", cursor.getString(0))
            assertEquals("book-1", cursor.getString(1))
            assertEquals("Fear is the mind-killer.", cursor.getString(2))
            val expectedEpochMillis = java.time.LocalDateTime.parse(
                "08-23-2026 12:00 PM",
                java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a", java.util.Locale.US)
            ).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            assertEquals(expectedEpochMillis, cursor.getLong(3))
        }
    }

    @Test
    fun migrate1To2_fallsBackToEpochZeroForUnparseableLegacyTimestamp() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-bad-timestamp', 'Title', NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL, NULL)"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-unparseable', 'book-bad-timestamp', 'text', 'not-a-real-timestamp')"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query(
            "SELECT id, text, savedOnEpochMillis FROM HighlightEntity WHERE id = 'highlight-unparseable'"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("highlight-unparseable", cursor.getString(0))
            assertEquals("text", cursor.getString(1))
            assertEquals(0L, cursor.getLong(2))
        }
    }

    @Test
    fun migrate1To2_convertsPipeDelimitedAuthorsWithLiteralPipeCharacter() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-pipe-author', 'Title', NULL, 'Foo|Bar', NULL, NULL, NULL, NULL, NULL, NULL, NULL)"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query("SELECT authors FROM BookEntity WHERE id = 'book-pipe-author'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("""["Foo","Bar"]""", cursor.getString(0))
        }
    }

    @Test
    fun migrate1To2_convertsEmptyPipeDelimitedAuthorsToEmptyJsonArray() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-no-authors', 'Title', NULL, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL)"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query("SELECT authors FROM BookEntity WHERE id = 'book-no-authors'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("[]", cursor.getString(0))
        }
    }

    @Test
    fun migrate1To2_ordersHighlightsAcrossAYearBoundaryByEpochMillis() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-year-boundary', 'Title', NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL, NULL)"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-january', 'book-year-boundary', 'January highlight', '01-05-2026 09:00 AM')"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-december', 'book-year-boundary', 'December highlight', '12-20-2025 09:00 AM')"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query(
            "SELECT id FROM HighlightEntity WHERE bookId = 'book-year-boundary' ORDER BY savedOnEpochMillis ASC"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("highlight-december", cursor.getString(0))
            assertEquals(true, cursor.moveToNext())
            assertEquals("highlight-january", cursor.getString(0))
        }
    }

    @Test
    fun migrate1To2_ordersHighlightsAcrossNoonByEpochMillis() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                "INSERT INTO BookEntity (id, title, subtitle, authors, thumbnailLink, publisher, " +
                    "publishedDate, description, pageCount, averageRating, ratingsCount) VALUES " +
                    "('book-noon-boundary', 'Title', NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL, NULL)"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-morning', 'book-noon-boundary', 'Morning highlight', '06-15-2023 11:00 AM')"
            )
            execSQL(
                "INSERT INTO HighlightEntity (id, bookId, text, savedOnTimestamp) VALUES " +
                    "('highlight-afternoon', 'book-noon-boundary', 'Afternoon highlight', '06-15-2023 01:00 PM')"
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        migratedDb.query(
            "SELECT id FROM HighlightEntity WHERE bookId = 'book-noon-boundary' ORDER BY savedOnEpochMillis ASC"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("highlight-morning", cursor.getString(0))
            assertEquals(true, cursor.moveToNext())
            assertEquals("highlight-afternoon", cursor.getString(0))
        }
    }
}
