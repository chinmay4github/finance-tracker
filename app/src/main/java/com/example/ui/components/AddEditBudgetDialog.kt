package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.BudgetEntity
import com.example.ui.theme.AlertWarningAmber
import com.example.ui.theme.ExpenseRed
import com.example.util.CategoryConstants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditBudgetDialog(
  initialBudget: BudgetEntity? = null,
  onDismiss: () -> Unit,
  onSave: (category: String, limit: Double, thresholdPercent: Int) -> Unit,
  onDelete: ((BudgetEntity) -> Unit)? = null
) {
  var selectedCategory by remember {
    mutableStateOf(initialBudget?.category ?: "Food & Dining")
  }
  var limitText by remember {
    mutableStateOf(initialBudget?.monthlyLimit?.let { String.format("%.0f", it) } ?: "500")
  }
  var thresholdSlider by remember {
    mutableFloatStateOf(initialBudget?.alertThresholdPercent?.toFloat() ?: 80f)
  }

  val availableCategories = listOf("Overall") + CategoryConstants.EXPENSE_CATEGORIES.map { it.name }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialBudget == null) "Set Monthly Budget" else "Edit Budget: ${initialBudget.category}",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        // Category selection (only editable if creating new budget)
        if (initialBudget == null) {
          Text(
            text = "Category",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            availableCategories.take(7).forEach { cat ->
              val isSelected = selectedCategory == cat
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .clickable { selectedCategory = cat }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = cat,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Monthly Limit Input
        Text(
          text = "Monthly Spending Limit ($)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = limitText,
          onValueChange = { input ->
            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
              limitText = input
            }
          },
          leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
          placeholder = { Text("500") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_limit_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Alert Threshold
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Automated Alert Threshold",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${thresholdSlider.toInt()}%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AlertWarningAmber
          )
        }

        Slider(
          value = thresholdSlider,
          onValueChange = { thresholdSlider = it },
          valueRange = 50f..95f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = AlertWarningAmber,
            activeTrackColor = AlertWarningAmber
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = "You'll receive automated alerts when spending crosses ${thresholdSlider.toInt()}% of the limit.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      val limitNumber = limitText.toDoubleOrNull() ?: 0.0
      Button(
        onClick = {
          if (limitNumber > 0) {
            onSave(selectedCategory, limitNumber, thresholdSlider.toInt())
          }
        },
        enabled = limitNumber > 0,
        modifier = Modifier.testTag("save_budget_button")
      ) {
        Text("Save Budget")
      }
    },
    dismissButton = {
      Row {
        if (initialBudget != null && onDelete != null) {
          TextButton(
            onClick = { onDelete(initialBudget) },
            colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
          ) {
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(8.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}
