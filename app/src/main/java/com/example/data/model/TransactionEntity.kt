package com.example.data.model

enum class TransactionType {
  EXPENSE,
  INCOME
}

/**
 * High-level domain and UI model for transactions in the finance tracker.
 */
data class TransactionEntity(
  val id: Long = 0,
  val title: String,
  val amount: Double,
  val type: TransactionType,
  val category: String,
  val paymentMethod: String,
  val timestamp: Long = System.currentTimeMillis(),
  val note: String = ""
) {
  fun toTransaction(): Transaction = Transaction(
    id = id,
    amount = if (type == TransactionType.EXPENSE) -kotlin.math.abs(amount) else kotlin.math.abs(amount),
    category = category,
    date = timestamp,
    description = if (note.isNotBlank()) "$title - $note" else title
  )
}

fun Transaction.toTransactionEntity(): TransactionEntity {
  val isExpense = amount < 0
  val absAmount = kotlin.math.abs(amount)
  val parsedTitle = if (description.contains(" - ")) {
    description.substringBefore(" - ").trim()
  } else {
    description.ifBlank { category }
  }
  val parsedNote = if (description.contains(" - ")) {
    description.substringAfter(" - ").trim()
  } else {
    ""
  }

  return TransactionEntity(
    id = id,
    title = parsedTitle,
    amount = absAmount,
    type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
    category = category,
    paymentMethod = "General",
    timestamp = date,
    note = parsedNote
  )
}
