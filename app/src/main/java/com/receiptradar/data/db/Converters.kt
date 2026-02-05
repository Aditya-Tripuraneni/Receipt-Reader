package com.receiptradar.data.db

import androidx.room.TypeConverter
import java.math.BigDecimal

/**
 * Room TypeConverters for custom types.
 */
class Converters {
    
    // BigDecimal <-> String conversion
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()
    
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
    
    // List<Long> <-> String conversion for storing receipt IDs in insight payload
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? = value?.joinToString(",")
    
    @TypeConverter
    fun toLongList(value: String?): List<Long>? =
        value?.takeIf { it.isNotBlank() }?.split(",")?.map { it.toLong() }
    
    // List<String> <-> String conversion
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString("|||")
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.takeIf { it.isNotBlank() }?.split("|||")
}
