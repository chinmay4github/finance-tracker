package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
  private val dateTimeFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
  private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
  private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)

  fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

  fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

  fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Date(timestamp))

  fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))

  fun isCurrentMonth(timestamp: Long): Boolean {
    val now = Calendar.getInstance()
    val check = Calendar.getInstance().apply { timeInMillis = timestamp }
    return now.get(Calendar.YEAR) == check.get(Calendar.YEAR) &&
      now.get(Calendar.MONTH) == check.get(Calendar.MONTH)
  }

  fun getCurrentMonthRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.timeInMillis

    return Pair(start, end)
  }

  fun getDaysRemainingInCurrentMonth(): Int {
    val cal = Calendar.getInstance()
    val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    return (totalDays - currentDay).coerceAtLeast(1)
  }

  fun getTotalDaysInCurrentMonth(): Int {
    return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  fun getCurrentDayOfMonth(): Int {
    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
  }

  fun formatFullDate(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))
}
