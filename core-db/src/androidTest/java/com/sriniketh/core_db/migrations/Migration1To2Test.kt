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
