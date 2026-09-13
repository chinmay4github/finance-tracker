package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
  private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 2
    minimumFractionDigits = 2
  }

  private val compactFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  fun formatCurrency(amount: Double): String {
    return currencyFormat.format(amount)
  }

  fun formatCompact(amount: Double): String {
    return when {
      amount >= 1_000_000 -> String.format(Locale.US, "$%.1fM", amount / 1_000_000)
      amount >= 10_000 -> String.format(Locale.US, "$%.1fk", amount / 1_000)
      else -> currencyFormat.format(amount)
    }
  }

  fun formatPercentage(percent: Double): String {
    return String.format(Locale.US, "%.0f%%", percent)
  }
}
