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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.remote.GoogleSheetSyncState
import com.example.ui.FinanceUiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CategoryConstants
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun TransactionsScreen(
  uiState: FinanceUiState,
  syncState: GoogleSheetSyncState? = null,
  onOpenSheetSync: () -> Unit = {},
  onSearchChanged: (String) -> Unit,
  onFilterTypeChanged: (TransactionType?) -> Unit,
  onFilterCategoryChanged: (String?) -> Unit,
  onMonthOffsetChanged: (Int) -> Unit,
  onAddTransactionClicked: () -> Unit,
  onEditTransactionClicked: (TransactionEntity) -> Unit,
  onDeleteTransactionClicked: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Screen Title & Month Navigator
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transactions",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Google Sheet quick sync button
            IconButton(
              onClick = onOpenSheetSync,
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                  if (syncState?.isConnected == true) Color(0xFFE6F4EA)
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .testTag("btn_transactions_sheet_sync")
            ) {
              Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = "Google Sheets Sync",
                tint = if (syncState?.isConnected == true) Color(0xFF0F9D58)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Month Switcher
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              IconButton(
                onClick = { onMonthOffsetChanged(uiState.selectedMonthOffset - 1) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Previous month",
                  modifier = Modifier.size(16.dp)
                )
              }

              Text(
                text = uiState.selectedMonthName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
              )

              IconButton(
                onClick = { onMonthOffsetChanged(uiState.selectedMonthOffset + 1) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "Next month",
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      // Search Field
      item {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onSearchChanged,
          placeholder = { Text("Search transactions, notes, categories...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChanged("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_search_input")
        )
      }

      // Filter Tabs (All / Expenses / Income)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterPill(
            title = "All (${uiState.filteredTransactions.size})",
            isSelected = uiState.filterType == null,
            onClick = { onFilterTypeChanged(null) },
            modifier = Modifier.weight(1f)
          )
          FilterPill(
            title = "Expenses",
            isSelected = uiState.filterType == TransactionType.EXPENSE,
            onClick = { onFilterTypeChanged(TransactionType.EXPENSE) },
            selectedColor = ExpenseRed,
            modifier = Modifier.weight(1f)
          )
          FilterPill(
            title = "Income",
            isSelected = uiState.filterType == TransactionType.INCOME,
            onClick = { onFilterTypeChanged(TransactionType.INCOME) },
            selectedColor = IncomeGreen,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Category Filter Chips
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              text = "All Categories",
              isSelected = uiState.filterCategory == null,
              onClick = { onFilterCategoryChanged(null) }
            )
          }

          items(CategoryConstants.ALL_CATEGORIES) { cat ->
            FilterChip(
              text = cat.name,
              isSelected = uiState.filterCategory == cat.name,
              onClick = {
                onFilterCategoryChanged(if (uiState.filterCategory == cat.name) null else cat.name)
              }
            )
          }
        }
      }

      // List or Empty state
      if (uiState.filteredTransactions.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No matching transactions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try adjusting your filters or record a new expense.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(uiState.filteredTransactions, key = { it.id }) { tx ->
          TransactionDetailedCard(
            transaction = tx,
            onEdit = { onEditTransactionClicked(tx) },
            onDelete = { transactionToDelete = tx }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = onAddTransactionClicked,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("fab_add_transaction"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp)
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Transaction")
    }
  }

  // Delete Confirmation Dialog
  transactionToDelete?.let { tx ->
    AlertDialog(
      onDismissRequest = { transactionToDelete = null },
      title = { Text("Delete Transaction") },
      text = { Text("Are you sure you want to delete '${tx.title}' for ${CurrencyUtils.formatCurrency(tx.amount)}?") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteTransactionClicked(tx)
            transactionToDelete = null
          },
          colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
        ) {
          Text("Delete", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { transactionToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun FilterPill(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  selectedColor: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(
        if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant
      )
      .clickable { onClick() }
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun FilterChip(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
      )
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun TransactionDetailedCard(
  transaction: TransactionEntity,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val catInfo = CategoryConstants.getCategoryInfo(transaction.category)
  val isExpense = transaction.type == TransactionType.EXPENSE

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .testTag("transaction_detail_card_${transaction.id}"),
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
            .size(40.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = transaction.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${transaction.category} • ${DateUtils.formatDateTime(transaction.timestamp)}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        val amountPrefix = if (isExpense) "-" else "+"
        val amountColor = if (isExpense) ExpenseRed else IncomeGreen
        Text(
          text = amountPrefix + CurrencyUtils.formatCurrency(transaction.amount),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = amountColor
        )
      }

      if (transaction.note.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "“${transaction.note}”",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = transaction.paymentMethod,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row {
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = ExpenseRed.copy(alpha = 0.8f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
