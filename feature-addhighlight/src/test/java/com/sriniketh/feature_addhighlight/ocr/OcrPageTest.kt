package com.sriniketh.feature_addhighlight.ocr

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OcrPageTest {

    private fun line(text: String, block: Int, left: Float, top: Float, right: Float, bottom: Float) =
        OcrLine(
            text = text,
            blockIndex = block,
            corners = listOf(
                Offset(left, top),
                Offset(right, top),
                Offset(right, bottom),
                Offset(left, bottom)
            )
        )

    private val page = OcrPage(
        width = 1000,
        height = 1000,
        lines = listOf(
            line("It is a truth universally", 0, 100f, 100f, 900f, 140f),
            line("acknowledged, that a single", 0, 100f, 150f, 900f, 190f),
            line("man must be in want of a wife.", 0, 100f, 200f, 700f, 240f),
            line("However little known the", 1, 100f, 300f, 900f, 340f),
            line("feelings of such a neigh-", 1, 100f, 350f, 900f, 390f),
            line("bourhood may be.", 1, 100f, 400f, 500f, 440f),
            line("my dear Mr. Bennet", 2, 100f, 500f, 900f, 540f)
        )
    )

    @Test
    fun `blocks group consecutive lines by their block`() {
        assertEquals(listOf(0..2, 3..5, 6..6), page.blocks)
    }

    @Test
    fun `lines of one block are joined with spaces`() {
        assertEquals(
            "It is a truth universally acknowledged, that a single man must be in want of a wife.",
            page.textOf(setOf(0, 1, 2))
        )
    }

    @Test
    fun `a word hyphenated across lines is joined without the hyphen`() {
        assertEquals("feelings of such a neighbourhood may be.", page.textOf(setOf(4, 5)))
    }

    @Test
    fun `a hyphen before a capitalised line is kept`() {
        val hyphenated = OcrPage(
            width = 100,
            height = 100,
            lines = listOf(
                line("the Anglo-", 0, 0f, 0f, 50f, 10f),
                line("Saxon kings", 0, 0f, 20f, 50f, 30f)
            )
        )

        assertEquals("the Anglo- Saxon kings", hyphenated.textOf(setOf(0, 1)))
    }

    @Test
    fun `a block that ends a sentence starts the next block on a new line`() {
        assertEquals(
            "man must be in want of a wife.\nHowever little known the",
            page.textOf(setOf(2, 3))
        )
        assertEquals("bourhood may be.\nmy dear Mr. Bennet", page.textOf(setOf(5, 6)))
    }

    @Test
    fun `a block that ends mid sentence continues the next block on the same line`() {
        assertEquals("However little known the my dear Mr. Bennet", page.textOf(setOf(3, 6)))
    }

    @Test
    fun `text follows reading order whatever order lines were selected in`() {
        assertEquals(page.textOf(setOf(0, 1)), page.textOf(linkedSetOf(1, 0)))
    }

    @Test
    fun `nothing selected gives empty text`() {
        assertEquals("", page.textOf(emptySet()))
    }

    @Test
    fun `a point inside a block finds that block`() {
        assertEquals(3..5, page.blockAt(Offset(800f, 420f), tolerance = 0f))
    }

    @Test
    fun `a point just outside a block finds it within the tolerance`() {
        assertEquals(6..6, page.blockAt(Offset(500f, 550f), tolerance = 12f))
        assertNull(page.blockAt(Offset(500f, 550f), tolerance = 5f))
    }

    @Test
    fun `a point away from all text finds no block`() {
        assertNull(page.blockAt(Offset(500f, 900f), tolerance = 12f))
    }

    @Test
    fun `the line nearest to a point between lines is the closer one`() {
        assertEquals(1, page.lineNearest(Offset(500f, 192f)))
        assertEquals(2, page.lineNearest(Offset(500f, 198f)))
    }

    @Test
    fun `the line nearest to a point beside a short line is that line`() {
        assertEquals(5, page.lineNearest(Offset(800f, 420f)))
    }

    @Test
    fun `the line nearest to a point inside two overlapping lines is the one centred closer`() {
        val skewed = OcrPage(
            width = 100,
            height = 100,
            lines = listOf(
                line("first", 0, 0f, 0f, 100f, 30f),
                line("second", 0, 0f, 20f, 100f, 50f)
            )
        )

        assertEquals(0, skewed.lineNearest(Offset(50f, 24f)))
        assertEquals(1, skewed.lineNearest(Offset(50f, 26f)))
    }

    @Test
    fun `a page without lines has no nearest line`() {
        assertNull(OcrPage(width = 10, height = 10, lines = emptyList()).lineNearest(Offset.Zero))
    }
}
