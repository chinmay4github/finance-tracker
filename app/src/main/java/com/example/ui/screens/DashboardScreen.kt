package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.remote.GoogleSheetSyncState
import com.example.ui.FinanceUiState
import com.example.ui.components.BudgetAlertBanner
import com.example.ui.components.CategoryDonutChart
import com.example.ui.theme.Emerald40
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CategoryConstants
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
  uiState: FinanceUiState,
  syncState: GoogleSheetSyncState? = null,
  onOpenSheetSync: () -> Unit = {},
  onAddTransactionClicked: () -> Unit,
  onViewAllTransactionsClicked: () -> Unit,
  onViewBudgetsClicked: () -> Unit,
  onTransactionClicked: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // App Header with Month and Google Sheets Cloud Sync Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Personal Finances",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = uiState.selectedMonthName,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Google Sheet Cloud Sync status pill
        Surface(
          onClick = onOpenSheetSync,
          shape = RoundedCornerShape(20.dp),
          color = if (syncState?.isConnected == true) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(
            1.dp,
            if (syncState?.isConnected == true) Color(0xFF0F9D58) else MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier.testTag("btn_dashboard_sheet_sync")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.TableChart,
              contentDescription = "Google Sheets Sync",
              tint = if (syncState?.isConnected == true) Color(0xFF0B8043) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (syncState?.isSyncing == true) "Syncing..."
                     else if (syncState?.isConnected == true) "Sheet API"
                     else "Connect Sheet",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (syncState?.isConnected == true) Color(0xFF0B8043) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (syncState?.isConnected == true) {
              Spacer(modifier = Modifier.width(5.dp))
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0F9D58))
              )
            }
          }
        }
      }
    }

    // Automated Budget Alerts Banner (if any warning or exceeded)
    item {
      BudgetAlertBanner(
        alerts = uiState.activeAlerts,
        onViewBudgetsClicked = onViewBudgetsClicked
      )
    }

    // Hero Financial Summary Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .testTag("hero_financial_card"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(EmeraldDark, Emerald40)
              )
            )
            .padding(20.dp)
        ) {
          Column {
            Text(
              text = "Net Cash Flow (${uiState.selectedMonthName})",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            val isPositive = uiState.netSavings >= 0
            Text(
              text = (if (isPositive) "+" else "") + CurrencyUtils.formatCurrency(uiState.netSavings),
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Income & Expense Sub-Pills
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Income Box
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.15f))
                  .padding(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowDownward,
                      contentDescription = "Income",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Income",
                      fontSize = 11.sp,
                      color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                      text = CurrencyUtils.formatCurrency(uiState.currentMonthIncome),
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }

              // Expenses Box
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.15f))
                  .padding(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = "Expense",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Expenses",
                      fontSize = 11.sp,
                      color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                      text = CurrencyUtils.formatCurrency(uiState.currentMonthExpenses),
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Quick Actions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onAddTransactionClicked,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_add_expense_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Record", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onViewBudgetsClicked,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_budget_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          Text("Budgets & Alerts", fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Google Sheets Cloud API Real-Time Sync Banner Card
    item {
      Card(
        onClick = onOpenSheetSync,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_google_sheet_sync_status"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (syncState?.isConnected == true) Color(0xFFF1F8F4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
          1.dp,
          if (syncState?.isConnected == true) Color(0xFF0F9D58).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (syncState?.isConnected == true) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.TableChart,
              contentDescription = null,
              tint = if (syncState?.isConnected == true) Color(0xFF0F9D58) else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Google Sheets Real-Time API",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (syncState?.isRealtimeSyncEnabled == true) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFE6F4EA)
                ) {
                  Text(
                    text = "LIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F9D58),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Text(
              text = if (syncState?.isConnected == true)
                "Room DB syncing to Google Sheet • ${syncState.lastSyncStatus}"
              else
                "Tap to connect your Google Sheet script for live backup",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Category Expense Breakdown (Donut Chart)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Spending Breakdown",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = "${uiState.categorySpendMap.size} categories",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          CategoryDonutChart(
            categorySpendMap = uiState.categorySpendMap,
            totalExpense = uiState.currentMonthExpenses
          )
        }
      }
    }

    // Recent Transactions Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        TextButton(onClick = onViewAllTransactionsClicked) {
          Text("View All (${uiState.filteredTransactions.size})")
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    val recentTransactions = uiState.filteredTransactions.take(5)
    if (recentTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No transactions recorded yet",
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(recentTransactions, key = { it.id }) { tx ->
        TransactionRowItem(
          transaction = tx,
          onClick = { onTransactionClicked(tx) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
    }
  }
}

@Composable
fun TransactionRowItem(
  transaction: TransactionEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val catInfo = CategoryConstants.getCategoryInfo(transaction.category)
  val isExpense = transaction.type == TransactionType.EXPENSE

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag("transaction_item_${transaction.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(catInfo.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = catInfo.icon,
          contentDescription = catInfo.name,
          tint = catInfo.color,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${transaction.category} • ${DateUtils.formatShortDate(transaction.timestamp)}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        val amountPrefix = if (isExpense) "-" else "+"
        val amountColor = if (isExpense) ExpenseRed else IncomeGreen

        Text(
          text = amountPrefix + CurrencyUtils.formatCurrency(transaction.amount),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = amountColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = transaction.paymentMethod,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
