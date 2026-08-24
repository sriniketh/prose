package com.sriniketh.feature_addhighlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageRotaterTest {

    @Test
    fun `when source is smaller than target then sample size is 1`() {
        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = 800,
            sourceHeight = 600,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun `when source equals target then sample size is 1`() {
        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = ImageRotater.TARGET_LONG_EDGE_PX,
            sourceHeight = ImageRotater.TARGET_LONG_EDGE_PX,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun `when source is a 12 megapixel photo then result is downsampled below target`() {
        val sourceWidth = 4032
        val sourceHeight = 3024

        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertTrue(sampleSize > 1)
        assertTrue(sourceWidth / sampleSize <= ImageRotater.TARGET_LONG_EDGE_PX)
        assertTrue(sourceHeight / sampleSize <= ImageRotater.TARGET_LONG_EDGE_PX)
    }

    @Test
    fun `when source is a 108 megapixel photo then result is bounded to target`() {
        val sourceWidth = 12000
        val sourceHeight = 9000

        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        val resultingLongEdge = maxOf(sourceWidth, sourceHeight) / sampleSize
        assertTrue(resultingLongEdge <= ImageRotater.TARGET_LONG_EDGE_PX)
    }

    @Test
    fun `when portrait source is larger than target then sample size is based on the long edge`() {
        val sourceWidth = 3024
        val sourceHeight = 4032

        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertTrue(sourceHeight / sampleSize <= ImageRotater.TARGET_LONG_EDGE_PX)
    }

    @Test
    fun `when source dimensions are unknown then sample size defaults to 1`() {
        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = -1,
            sourceHeight = -1,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun `sample size is always a power of two`() {
        val sampleSize = ImageRotater.calculateInSampleSize(
            sourceWidth = 12000,
            sourceHeight = 9000,
            targetSize = ImageRotater.TARGET_LONG_EDGE_PX
        )

        assertEquals(0, sampleSize and (sampleSize - 1))
    }
}
