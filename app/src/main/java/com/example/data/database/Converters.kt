package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.TransactionType

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
}
