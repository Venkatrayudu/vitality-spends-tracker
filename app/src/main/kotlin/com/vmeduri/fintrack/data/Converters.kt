package com.vmeduri.fintrack.data

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Room only knows how to store its own supported column types (Long, String, etc.), so
 * it needs to be told explicitly how to turn a [LocalDate] and a [Category] into
 * something a SQLite column can hold, and back again.
 */
class Converters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromCategoryName(name: String?): Category? = name?.let { Category.valueOf(it) }

    @TypeConverter
    fun toCategoryName(category: Category?): String? = category?.name
}
