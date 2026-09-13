package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.theme.AlertWarningAmber
import com.example.ui.theme.Emerald40
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CategoryConstants
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun AnalyticsScreen(
  uiState: FinanceUiState,
  modifier: Modifier = Modifier
) {
  val totalIncome = uiState.currentMonthIncome
  val totalExpense = uiState.currentMonthExpenses
  val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome) * 100.0 else 0.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Column {
        Text(
          text = "Financial Analytics",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Spending trends & savings intelligence (${uiState.selectedMonthName})",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Savings Rate & Health Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .testTag("savings_rate_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(IncomeGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = IncomeGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Monthly Savings Rate",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = String.format("%.1f%%", savingsRate.coerceAtLeast(0.0)),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = if (savingsRate >= 20.0) IncomeGreen else if (savingsRate >= 0) AlertWarningAmber else ExpenseRed
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Visual Cash Flow Ratio
          val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
          val incomeRatio = (totalIncome / totalFlow).toFloat().coerceIn(0f, 1f)

          LinearProgressIndicator(
            progress = { incomeRatio },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = IncomeGreen,
            trackColor = ExpenseRed.copy(alpha = 0.8f),
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Income: " + CurrencyUtils.formatCurrency(totalIncome),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = IncomeGreen
            )
            Text(
              text = "Spent: " + CurrencyUtils.formatCurrency(totalExpense),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = ExpenseRed
            )
          }
        }
      }
    }

    // Category Spending Distribution Bars
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Category Ranking",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(14.dp))

          val sortedCategories = uiState.categorySpendMap.entries.sortedByDescending { it.value }
          if (sortedCategories.isEmpty()) {
            Text(
              text = "No category data available for this month.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              sortedCategories.forEach { (catName, amount) ->
                val catInfo = CategoryConstants.getCategoryInfo(catName)
                val percent = if (totalExpense > 0) (amount / totalExpense) * 100.0 else 0.0

                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(catInfo.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = catInfo.icon,
                          contentDescription = null,
                          tint = catInfo.color,
                          modifier = Modifier.size(14.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = catName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    }

                    Text(
                      text = "${CurrencyUtils.formatCurrency(amount)} (${percent.toInt()}%)",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  LinearProgressIndicator(
                    progress = { (percent / 100f).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = catInfo.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                  )
                }
              }
            }
          }
        }
      }
    }

    // Payment Method Breakdown
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Payment Method Share",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          val methodExpenses = uiState.filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.paymentMethod }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

          if (methodExpenses.isEmpty()) {
            Text(
              text = "No transaction data available.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              methodExpenses.forEach { (method, amount) ->
                val pct = if (totalExpense > 0) (amount / totalExpense) * 100.0 else 0.0
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = method, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                  Text(
                    text = "${CurrencyUtils.formatCurrency(amount)} (${pct.toInt()}%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // Automated Budget Intelligence & Tips
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Automated Budget Advice",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          val topSpend = uiState.categorySpendMap.maxByOrNull { it.value }
          if (topSpend != null) {
            Text(
              text = "• Top Spending Driver: '${topSpend.key}' accounts for ${CurrencyUtils.formatCurrency(topSpend.value)}. Keeping this category under control will have the largest impact on your savings.",
              fontSize = 12.sp,
              lineHeight = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          val daysLeft = DateUtils.getDaysRemainingInCurrentMonth()
          val currentDay = DateUtils.getCurrentDayOfMonth()
          val dailyAverage = if (currentDay > 0) totalExpense / currentDay else 0.0
          Text(
            text = "• Daily Burn Rate: You are averaging ${CurrencyUtils.formatCurrency(dailyAverage)}/day. There are $daysLeft days remaining in the month.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(88.dp))
    }
  }
}
