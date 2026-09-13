package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.TransactionType
import java.util.Date

class Converters {
  @TypeConverter
  fun fromTransactionType(type: TransactionType): String {
    return type.name
  }

  @TypeConverter
  fun toTransactionType(value: String): TransactionType {
    return try {
      TransactionType.valueOf(value)
    } catch (e: Exception) {
      TransactionType.EXPENSE
    }
  }

  @TypeConverter
  fun fromTimestamp(value: Long?): Date? {
    return value?.let { Date(it) }
  }

  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time
  }
}
