package com.sriniketh.core_db.converters

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ListTypeConverterTest {

    private lateinit var converter: ListTypeConverter

    @Before
    fun setup() {
        converter = ListTypeConverter()
    }

    @Test
    fun `fromList creates string from list of strings`() {
        val list = listOf("string1", "string2", "string3")
        val result = converter.fromList(list)
        assertEquals("string1|string2|string3", result)
    }

    @Test
    fun `fromList creates empty string if list of strings passed is empty`() {
        assertEquals("", converter.fromList(emptyList()))
    }

    @Test
    fun `toList creates list of strings from string`() {
        val result = converter.toList("string1|string2|string3")
        assertEquals(listOf("string1", "string2", "string3"), result)
    }

    @Test
    fun `toList creates list of empty string if string passed is empty`() {
        assertEquals(listOf(""), converter.toList(""))
    }
}