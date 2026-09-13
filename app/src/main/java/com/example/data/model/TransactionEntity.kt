package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
  EXPENSE,
  INCOME
}

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val amount: Double,
  val type: TransactionType,
  val category: String,
  val paymentMethod: String,
  val timestamp: Long = System.currentTimeMillis(),
  val note: String = ""
)
