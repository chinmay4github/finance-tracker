package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetAlert
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.AlertDangerBg
import com.example.ui.theme.AlertDangerRed
import com.example.ui.theme.AlertWarningAmber
import com.example.ui.theme.AlertWarningBg
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CategoryConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
  initialTransaction: TransactionEntity? = null,
  onDismiss: () -> Unit,
  onSave: (TransactionEntity) -> Unit,
  onSimulateAlert: (category: String, amount: Double) -> BudgetAlert?,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
  var amountText by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
  var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
  var selectedCategory by remember {
    mutableStateOf(
      initialTransaction?.category ?: if (type == TransactionType.EXPENSE) "Food & Dining" else "Salary & Wages"
    )
  }
  var selectedPaymentMethod by remember {
    mutableStateOf(initialTransaction?.paymentMethod ?: "Credit Card")
  }
  var note by remember { mutableStateOf(initialTransaction?.note ?: "") }

  // Quick suggestions based on type
  val titleSuggestions = if (type == TransactionType.EXPENSE) {
    listOf("Groceries", "Uber/Taxi", "Coffee", "Restaurant", "Gas", "Pharmacy", "Shopping", "Rent")
  } else {
    listOf("Salary", "Freelance", "Bonus", "Dividend", "Reimbursement", "Gift")
  }

  val amountNumber = amountText.toDoubleOrNull() ?: 0.0
  val simulatedAlert = if (type == TransactionType.EXPENSE) {
    onSimulateAlert(selectedCategory, amountNumber)
  } else null

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("add_edit_transaction_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (initialTransaction == null) "Add Transaction" else "Edit Transaction",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Type Selector Tabs (Expense / Income)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(4.dp)
      ) {
        // Expense Tab
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
            .clickable {
              type = TransactionType.EXPENSE
              if (selectedCategory in CategoryConstants.INCOME_CATEGORIES.map { it.name }) {
                selectedCategory = "Food & Dining"
              }
            }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Expense",
            fontWeight = FontWeight.Bold,
            color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
          )
        }

        // Income Tab
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (type == TransactionType.INCOME) IncomeGreen else Color.Transparent)
            .clickable {
              type = TransactionType.INCOME
              if (selectedCategory in CategoryConstants.EXPENSE_CATEGORIES.map { it.name }) {
                selectedCategory = "Salary & Wages"
              }
            }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Income",
            fontWeight = FontWeight.Bold,
            color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Amount Input
      Text(
        text = "Amount",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = amountText,
        onValueChange = { input ->
          if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
            amountText = input
          }
        },
        leadingIcon = {
          Text(
            text = "$",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
          )
        },
        placeholder = { Text("0.00", fontSize = 24.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("amount_input")
      )

      // Quick Amount Presets
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(10, 25, 50, 100).forEach { preset ->
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .clickable {
                val current = amountText.toDoubleOrNull() ?: 0.0
                amountText = String.format("%.2f", current + preset)
              }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "+$$preset",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Inline Automated Budget Alert Simulation Warning
      AnimatedVisibility(visible = simulatedAlert != null) {
        simulatedAlert?.let { alert ->
          Spacer(modifier = Modifier.height(12.dp))
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (alert.percentageUsed >= 100) AlertDangerBg else AlertWarningBg
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (alert.percentageUsed >= 100) AlertDangerRed else AlertWarningAmber
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (alert.percentageUsed >= 100) AlertDangerRed else AlertWarningAmber,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = alert.message,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (alert.percentageUsed >= 100) AlertDangerRed else AlertWarningAmber
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Title & Suggestions
      Text(
        text = "Title / Merchant",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g. Whole Foods, Monthly Salary") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("title_input")
      )

      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        titleSuggestions.take(5).forEach { suggestion ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
              .clickable { title = suggestion }
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = suggestion,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Selection Grid
      Text(
        text = "Category",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      val categoriesToShow = if (type == TransactionType.EXPENSE) {
        CategoryConstants.EXPENSE_CATEGORIES
      } else {
        CategoryConstants.INCOME_CATEGORIES
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categoriesToShow.forEach { cat ->
          val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSelected) cat.color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) cat.color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
              )
              .clickable { selectedCategory = cat.name }
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(cat.color.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = cat.icon,
                contentDescription = null,
                tint = cat.color,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = cat.name,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Payment Method Selection
      Text(
        text = "Payment Method",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        CategoryConstants.PAYMENT_METHODS.take(4).forEach { method ->
          val isSelected = selectedPaymentMethod == method
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
              )
              .clickable { selectedPaymentMethod = method }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = method,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Note (Optional)
      Text(
        text = "Note (Optional)",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        placeholder = { Text("Add an optional memo or detail...") },
        singleLine = false,
        maxLines = 2,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      val isFormValid = amountNumber > 0 && title.isNotBlank()
      Button(
        onClick = {
          if (isFormValid) {
            val tx = initialTransaction?.copy(
              title = title.trim(),
              amount = amountNumber,
              type = type,
              category = selectedCategory,
              paymentMethod = selectedPaymentMethod,
              note = note.trim()
            ) ?: TransactionEntity(
              title = title.trim(),
              amount = amountNumber,
              type = type,
              category = selectedCategory,
              paymentMethod = selectedPaymentMethod,
              note = note.trim()
            )
            onSave(tx)
          }
        },
        enabled = isFormValid,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_transaction_button")
      ) {
        Text(
          text = if (initialTransaction == null) "Save Transaction" else "Update Transaction",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
