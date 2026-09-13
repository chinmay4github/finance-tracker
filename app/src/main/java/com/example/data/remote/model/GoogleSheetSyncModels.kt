package com.example.data.remote.model

import com.example.data.model.Transaction
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.DateUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object (DTO) for transactions synced to/from Google Sheets.
 */
@JsonClass(generateAdapter = true)
data class GoogleSheetTransactionDto(
  @field:Json(name = "id") val id: Long = 0L,
  @field:Json(name = "title") val title: String = "",
  @field:Json(name = "amount") val amount: Double = 0.0,
  @field:Json(name = "type") val type: String = "EXPENSE",
  @field:Json(name = "category") val category: String = "General",
  @field:Json(name = "paymentMethod") val paymentMethod: String = "Cash",
  @field:Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
  @field:Json(name = "dateFormatted") val dateFormatted: String? = null,
  @field:Json(name = "note") val note: String = ""
) {
  fun toTransactionEntity(): TransactionEntity {
    val txType = try {
      TransactionType.valueOf(type.uppercase())
    } catch (e: Exception) {
      TransactionType.EXPENSE
    }
    return TransactionEntity(
      id = id,
      title = title.ifBlank { category },
      amount = kotlin.math.abs(amount),
      type = txType,
      category = category.ifBlank { "General" },
      paymentMethod = paymentMethod.ifBlank { "Cash" },
      timestamp = timestamp,
      note = note
    )
  }

  fun toTransaction(): Transaction {
    val isExpense = type.equals("EXPENSE", ignoreCase = true)
    val finalAmount = if (isExpense) -kotlin.math.abs(amount) else kotlin.math.abs(amount)
    val desc = when {
      title.isNotBlank() && note.isNotBlank() -> "$title - $note"
      title.isNotBlank() -> title
      note.isNotBlank() -> note
      else -> category
    }
    return Transaction(
      id = id,
      amount = finalAmount,
      category = category.ifBlank { "General" },
      date = timestamp,
      description = desc
    )
  }
}

/**
 * Request payload sent to Google Apps Script Web App.
 */
@JsonClass(generateAdapter = true)
data class GoogleSheetSyncRequest(
  @field:Json(name = "action") val action: String,
  @field:Json(name = "transaction") val transaction: GoogleSheetTransactionDto? = null,
  @field:Json(name = "transactions") val transactions: List<GoogleSheetTransactionDto>? = null,
  @field:Json(name = "id") val id: Long? = null
)

/**
 * Response payload received from Google Apps Script Web App.
 */
@JsonClass(generateAdapter = true)
data class GoogleSheetSyncResponse(
  @field:Json(name = "status") val status: String? = null,
  @field:Json(name = "message") val message: String? = null,
  @field:Json(name = "spreadsheetName") val spreadsheetName: String? = null,
  @field:Json(name = "count") val count: Int? = null,
  @field:Json(name = "transactions") val transactions: List<GoogleSheetTransactionDto>? = null,
  @field:Json(name = "lastUpdated") val lastUpdated: String? = null
)

// Extension mapper functions
fun TransactionEntity.toGoogleSheetDto(): GoogleSheetTransactionDto = GoogleSheetTransactionDto(
  id = id,
  title = title,
  amount = amount,
  type = type.name,
  category = category,
  paymentMethod = paymentMethod,
  timestamp = timestamp,
  dateFormatted = DateUtils.formatFullDate(timestamp),
  note = note
)

fun Transaction.toGoogleSheetDto(): GoogleSheetTransactionDto = GoogleSheetTransactionDto(
  id = id,
  title = description.substringBefore(" - ").ifBlank { category },
  amount = kotlin.math.abs(amount),
  type = if (amount < 0) "EXPENSE" else "INCOME",
  category = category,
  paymentMethod = "Cash",
  timestamp = date,
  dateFormatted = DateUtils.formatFullDate(date),
  note = if (description.contains(" - ")) description.substringAfter(" - ") else ""
)
