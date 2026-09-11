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
    fun `fromList creates json array from list of strings`() {
        val list = listOf("string1", "string2", "string3")
        val result = converter.fromList(list)
        assertEquals("""["string1","string2","string3"]""", result)
    }

    @Test
    fun `fromList creates empty json array if list of strings passed is empty`() {
        assertEquals("[]", converter.fromList(emptyList()))
    }

    @Test
    fun `fromList preserves an author name containing the legacy pipe delimiter`() {
        val list = listOf("Bar|Baz")
        val result = converter.fromList(list)
        assertEquals(listOf("Bar|Baz"), converter.toList(result))
    }

    @Test
    fun `toList creates list of strings from json array`() {
        val result = converter.toList("""["string1","string2","string3"]""")
        assertEquals(listOf("string1", "string2", "string3"), result)
    }

    @Test
    fun `toList creates empty list if json array passed is empty`() {
        assertEquals(emptyList<String>(), converter.toList("[]"))
    }

    @Test
    fun `fromList then toList round trips an empty list back to an empty list`() {
        val result = converter.toList(converter.fromList(emptyList()))
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `fromList then toList round trips a list containing a pipe character author`() {
        val list = listOf("Bar|Baz", "Foo")
        val result = converter.toList(converter.fromList(list))
        assertEquals(list, result)
    }
}
