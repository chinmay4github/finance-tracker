package com.example.data.model

enum class AlertSeverity {
  SAFE, // Under alert threshold
  WARNING, // Exceeded threshold (e.g. >= 80% and < 100%)
  EXCEEDED // At or exceeded 100% of budget limit
}

data class BudgetAlert(
  val category: String,
  val monthlyLimit: Double,
  val spentAmount: Double,
  val percentageUsed: Double,
  val alertThresholdPercent: Int,
  val severity: AlertSeverity,
  val message: String,
  val remainingBudget: Double,
  val projectedMonthEndSpend: Double,
  val recommendedDailyCap: Double,
  val daysRemainingInMonth: Int
)
