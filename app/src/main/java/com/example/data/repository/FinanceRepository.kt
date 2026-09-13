package com.example.data.repository

import com.example.data.SampleData
import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FinanceRepository(
  private val transactionDao: TransactionDao,
  private val budgetDao: BudgetDao
) {
  val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
  val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

  suspend fun insertTransaction(transaction: TransactionEntity): Long =
    transactionDao.insertTransaction(transaction)

  suspend fun insertTransactions(transactions: List<TransactionEntity>) =
    transactionDao.insertTransactions(transactions)

  suspend fun getAllTransactionsSnapshot(): List<TransactionEntity> =
    transactionDao.getAllTransactions().first()

  suspend fun updateTransaction(transaction: TransactionEntity) =
    transactionDao.updateTransaction(transaction)

  suspend fun deleteTransaction(transaction: TransactionEntity) =
    transactionDao.deleteTransaction(transaction)

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
      transactionDao.insertTransactions(SampleData.getInitialTransactions())
    }
  }

  suspend fun resetToDemoData() {
    transactionDao.clearAll()
    budgetDao.clearAll()
    budgetDao.insertBudgets(SampleData.getInitialBudgets())
    transactionDao.insertTransactions(SampleData.getInitialTransactions())
  }

  suspend fun clearAllData() {
    transactionDao.clearAll()
    budgetDao.clearAll()
  }
}
