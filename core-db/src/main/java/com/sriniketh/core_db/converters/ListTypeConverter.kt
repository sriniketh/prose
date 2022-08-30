package com.sriniketh.core_db.converters

import androidx.room.TypeConverter

class ListTypeConverter {

    private val delimiter = "|"

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(delimiter)
    }

    @TypeConverter
    fun toList(string: String): List<String> {
        return string.split(delimiter)
    }
}
