package com.example.data.repository

import com.example.data.SampleData
import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.Transaction
import com.example.data.model.TransactionEntity
import com.example.data.model.toTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FinanceRepository(
  private val transactionDao: TransactionDao,
  private val budgetDao: BudgetDao
) {
  // Raw Transaction entity flow
  val rawTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

  // Domain model flow for UI
  val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions().map { list ->
    list.map { it.toTransactionEntity() }
  }

  val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

  // Raw Transaction CRUD operations
  suspend fun insertRawTransaction(transaction: Transaction): Long =
    transactionDao.insertTransaction(transaction)

  suspend fun updateRawTransaction(transaction: Transaction) =
    transactionDao.updateTransaction(transaction)

  suspend fun deleteRawTransaction(transaction: Transaction) =
    transactionDao.deleteTransaction(transaction)

  // TransactionEntity CRUD operations
  suspend fun insertTransaction(transaction: TransactionEntity): Long =
    transactionDao.insertTransaction(transaction.toTransaction())

  suspend fun insertTransactions(transactions: List<TransactionEntity>) =
    transactionDao.insertTransactions(transactions.map { it.toTransaction() })

  suspend fun getAllTransactionsSnapshot(): List<TransactionEntity> =
    transactionDao.getAllTransactions().first().map { it.toTransactionEntity() }

  suspend fun updateTransaction(transaction: TransactionEntity) =
    transactionDao.updateTransaction(transaction.toTransaction())

  suspend fun deleteTransaction(transaction: TransactionEntity) =
    transactionDao.deleteTransaction(transaction.toTransaction())

  suspend fun deleteTransactionById(id: Long) =
    transactionDao.deleteTransactionById(id)

  suspend fun insertBudget(budget: BudgetEntity): Long =
    budgetDao.insertBudget(budget)

  suspend fun updateBudget(budget: BudgetEntity) =
    budgetDao.updateBudget(budget)

  suspend fun deleteBudget(budget: BudgetEntity) =
    budgetDao.deleteBudget(budget)

  suspend fun deleteBudgetById(id: Long) =
    budgetDao.deleteBudgetById(id)

  suspend fun seedInitialDataIfEmpty() {
    val existingBudgets = budgetDao.getAllBudgets().first()
    if (existingBudgets.isEmpty()) {
      budgetDao.insertBudgets(SampleData.getInitialBudgets())
    }

    val existingTransactions = transactionDao.getAllTransactions().first()
    if (existingTransactions.isEmpty()) {
      transactionDao.insertTransactions(SampleData.getInitialTransactions().map { it.toTransaction() })
    }
  }

  suspend fun resetToDemoData() {
    transactionDao.clearAll()
    budgetDao.clearAll()
    budgetDao.insertBudgets(SampleData.getInitialBudgets())
    transactionDao.insertTransactions(SampleData.getInitialTransactions().map { it.toTransaction() })
  }

  suspend fun clearAllData() {
    transactionDao.clearAll()
    budgetDao.clearAll()
  }
}
