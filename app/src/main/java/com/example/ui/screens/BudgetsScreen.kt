package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.BudgetAlert
import com.example.data.model.BudgetEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.AlertDangerBg
import com.example.ui.theme.AlertDangerRed
import com.example.ui.theme.AlertSafeBg
import com.example.ui.theme.AlertSafeGreen
import com.example.ui.theme.AlertWarningAmber
import com.example.ui.theme.AlertWarningBg
import com.example.util.CategoryConstants
import com.example.util.CurrencyUtils

@Composable
fun BudgetsScreen(
  uiState: FinanceUiState,
  onAddBudgetClicked: () -> Unit,
  onEditBudgetClicked: (BudgetEntity) -> Unit,
  onResetDemoData: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeAlertsCount = uiState.activeAlerts.size
  val hasCritical = uiState.activeAlerts.any { it.severity == AlertSeverity.EXCEEDED }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Budgets & Alerts",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Automated spending limit monitor",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = onResetDemoData,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Reset Demo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Automated Engine Status Header Card
      item {
        val statusBg = when {
          hasCritical -> AlertDangerBg
          activeAlertsCount > 0 -> AlertWarningBg
          else -> AlertSafeBg
        }
        val statusColor = when {
          hasCritical -> AlertDangerRed
          activeAlertsCount > 0 -> AlertWarningAmber
          else -> AlertSafeGreen
        }
        val statusIcon = when {
          hasCritical -> Icons.Default.Warning
          activeAlertsCount > 0 -> Icons.Default.NotificationsActive
          else -> Icons.Default.CheckCircle
        }
        val statusTitle = when {
          hasCritical -> "$activeAlertsCount Budgets Exceeded Limit!"
          activeAlertsCount > 0 -> "$activeAlertsCount Budgets Near Threshold"
          else -> "All Budgets Within Safe Range"
        }
        val statusSubtitle = when {
          hasCritical -> "Immediate review recommended. Spending has exceeded allocated limits."
          activeAlertsCount > 0 -> "Automated thresholds breached. Review run-rate projections below."
          else -> "Your current spending pace is on track with your monthly targets."
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .testTag("budget_engine_status_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = statusBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = statusTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = statusSubtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      // Overall Monthly Budget Card
      uiState.overallAlert?.let { overall ->
        item {
          OverallBudgetCard(
            alert = overall,
            onEdit = {
              uiState.overallBudget?.let { onEditBudgetClicked(it) }
            }
          )
        }
      }

      // Category Budgets Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Category Budgets (${uiState.categoryAlerts.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Category Budget Cards
      if (uiState.categoryAlerts.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No category budgets configured yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))
              Button(
                onClick = onAddBudgetClicked,
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Set Your First Budget")
              }
            }
          }
        }
      } else {
        items(uiState.categoryAlerts, key = { it.category }) { alert ->
          val budgetEntity = uiState.budgets.find { it.category == alert.category }
          CategoryBudgetCard(
            alert = alert,
            onEdit = {
              budgetEntity?.let { onEditBudgetClicked(it) }
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    // Add Budget Floating Action Button
    FloatingActionButton(
      onClick = onAddBudgetClicked,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("fab_add_budget"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp)
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Budget")
    }
  }
}

@Composable
fun OverallBudgetCard(
  alert: BudgetAlert,
  onEdit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val progress = (alert.spentAmount / alert.monthlyLimit).toFloat().coerceIn(0f, 1f)
  val progressColor = when (alert.severity) {
    AlertSeverity.EXCEEDED -> AlertDangerRed
    AlertSeverity.WARNING -> AlertWarningAmber
    AlertSeverity.SAFE -> AlertSafeGreen
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .testTag("overall_budget_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Monthly Budget",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Alerts trigger at ${alert.alertThresholdPercent}% threshold",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit budget",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = CurrencyUtils.formatCurrency(alert.spentAmount),
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = progressColor
        )
        Text(
          text = "of " + CurrencyUtils.formatCurrency(alert.monthlyLimit),
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp)),
        color = progressColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Stats row: Projection and Daily Cap
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Month-End Run Rate",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatCurrency(alert.projectedMonthEndSpend),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Daily Safe Cap (${alert.daysRemainingInMonth}d left)",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatCurrency(alert.recommendedDailyCap) + "/day",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (alert.recommendedDailyCap > 0) AlertSafeGreen else AlertDangerRed
          )
        }
      }
    }
  }
}

@Composable
fun CategoryBudgetCard(
  alert: BudgetAlert,
  onEdit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val catInfo = CategoryConstants.getCategoryInfo(alert.category)
  val progress = (alert.spentAmount / alert.monthlyLimit).toFloat().coerceIn(0f, 1f)
  val isExceeded = alert.severity == AlertSeverity.EXCEEDED
  val isWarning = alert.severity == AlertSeverity.WARNING

  val badgeColor = when (alert.severity) {
    AlertSeverity.EXCEEDED -> AlertDangerRed
    AlertSeverity.WARNING -> AlertWarningAmber
    AlertSeverity.SAFE -> AlertSafeGreen
  }

  val badgeBg = when (alert.severity) {
    AlertSeverity.EXCEEDED -> AlertDangerBg
    AlertSeverity.WARNING -> AlertWarningBg
    AlertSeverity.SAFE -> AlertSafeBg
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .testTag("budget_card_${alert.category}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(catInfo.color.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = catInfo.icon,
            contentDescription = catInfo.name,
            tint = catInfo.color,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = alert.category,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Alert at ${alert.alertThresholdPercent}%",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Percentage Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${alert.percentageUsed.toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor
          )
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = badgeColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Amounts Spent vs Limit
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Spent: " + CurrencyUtils.formatCurrency(alert.spentAmount),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Limit: " + CurrencyUtils.formatCurrency(alert.monthlyLimit),
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Automated alert pill if threshold breached
      if (isExceeded || isWarning) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isExceeded) Icons.Default.Warning else Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = alert.message,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = badgeColor
          )
        }
      }

      // Run-rate projection helper
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Run-rate: ${CurrencyUtils.formatCompact(alert.projectedMonthEndSpend)} projected",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isExceeded) {
          Text(
            text = "${CurrencyUtils.formatCurrency(alert.recommendedDailyCap)}/day left",
            fontSize = 10.sp,
            color = AlertSafeGreen,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
