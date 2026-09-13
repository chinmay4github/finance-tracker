package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
  private const val CHANNEL_ID = "budget_alerts_channel"
  private const val CHANNEL_NAME = "Automated Budget Alerts"
  private const val CHANNEL_DESC = "Notifications for category and monthly budget thresholds"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = CHANNEL_DESC
        enableVibration(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun sendBudgetAlertNotification(
    context: Context,
    category: String,
    percentage: Int,
    spent: Double,
    limit: Double,
    isExceeded: Boolean
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val title = if (isExceeded) {
      "🚨 Budget Exceeded: $category"
    } else {
      "⚠️ Budget Alert: $category at $percentage%"
    }

    val formattedSpent = CurrencyUtils.formatCurrency(spent)
    val formattedLimit = CurrencyUtils.formatCurrency(limit)
    val text = if (isExceeded) {
      "You have spent $formattedSpent of your $formattedLimit budget ($percentage%)."
    } else {
      "You have reached $percentage% of your $formattedLimit budget ($formattedSpent spent)."
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_notify_error)
      .setContentTitle(title)
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = category.hashCode()
    manager.notify(notificationId, builder.build())
  }
}
