package com.example.todoapp.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String): LocalDate {
        return LocalDate.parse(value, formatter)
    }

    @TypeConverter
    fun localDateToString(date: LocalDate): String {
        return date.format(formatter)
    }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun stringListToString(list: List<String>): String {
        return list.joinToString(",")
    }
}
