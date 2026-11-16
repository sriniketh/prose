package com.sriniketh.core_platform

import org.junit.Assert.assertEquals
import org.junit.Test

class LogExtensionsTest {

    private class Widget

    @Test
    fun `logTag returns PROSE_DEBUG_LOG prefix with the receiver's simple class name`() {
        val widget = Widget()

        val tag = widget.logTag()

        assertEquals("PROSE_DEBUG_LOG: Widget", tag)
    }

    @Test
    fun `logTag uses the simple name of a different receiver type`() {
        val text = "some string"

        val tag = text.logTag()

        assertEquals("PROSE_DEBUG_LOG: String", tag)
    }

    @Test
    fun `logTag uses the reified type parameter not the runtime subtype`() {
        val number: Number = 42

        val tag = number.logTag()

        assertEquals("PROSE_DEBUG_LOG: Number", tag)
    }
}
