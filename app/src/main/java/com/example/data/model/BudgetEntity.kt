package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val category: String, // Category name, or "Overall" for total monthly limit
  val monthlyLimit: Double,
  val alertThresholdPercent: Int = 80, // percentage e.g. 80 means alert at 80%
  val isAlertEnabled: Boolean = true
)
