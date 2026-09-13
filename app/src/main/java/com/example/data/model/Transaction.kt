package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a financial transaction.
 *
 * @property id Unique identifier for the transaction (auto-generated primary key).
 * @property amount The monetary value of the transaction.
 * @property category The category of the transaction (e.g. Food, Transport, Salary).
 * @property date The epoch timestamp in milliseconds when the transaction occurred.
 * @property description A description or memo for the transaction.
 */
@Entity(tableName = "transactions")
data class Transaction(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val amount: Double,
  val category: String,
  val date: Long = System.currentTimeMillis(),
  val description: String = ""
)
