package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AlertSeverity
import com.example.data.model.BudgetAlert
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.remote.GoogleSheetSyncManager
import com.example.data.remote.GoogleSheetSyncState
import com.example.data.remote.GoogleSheetsApiClient
import com.example.data.repository.FinanceRepository
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinanceUiState(
  val allTransactions: List<TransactionEntity> = emptyList(),
  val filteredTransactions: List<TransactionEntity> = emptyList(),
  val budgets: List<BudgetEntity> = emptyList(),
  val currentMonthExpenses: Double = 0.0,
  val currentMonthIncome: Double = 0.0,
  val netSavings: Double = 0.0,
  val overallBudget: BudgetEntity? = null,
  val overallAlert: BudgetAlert? = null,
  val categoryAlerts: List<BudgetAlert> = emptyList(),
  val activeAlerts: List<BudgetAlert> = emptyList(),
  val categorySpendMap: Map<String, Double> = emptyMap(),
  val selectedMonthOffset: Int = 0,
  val selectedMonthName: String = "",
  val searchQuery: String = "",
  val filterType: TransactionType? = null,
  val filterCategory: String? = null,
  val isLoading: Boolean = true
)

private data class FilterParams(
  val query: String,
  val typeFilter: TransactionType?,
  val catFilter: String?,
  val monthOffset: Int
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: FinanceRepository
  private val sheetSyncManager: GoogleSheetSyncManager = GoogleSheetSyncManager(application)
  private val googleSheetsClient: GoogleSheetsApiClient = GoogleSheetsApiClient()

  val sheetSyncState: StateFlow<GoogleSheetSyncState> = sheetSyncManager.syncState

  private val _searchQuery = MutableStateFlow("")
  private val _filterType = MutableStateFlow<TransactionType?>(null)
  private val _filterCategory = MutableStateFlow<String?>(null)
  private val _selectedMonthOffset = MutableStateFlow(0) // 0 = current month

  init {
    val db = AppDatabase.getDatabase(application)
    repository = FinanceRepository(db.transactionDao(), db.budgetDao())
    NotificationHelper.createNotificationChannel(application)

    viewModelScope.launch {
      repository.seedInitialDataIfEmpty()
    }
  }

  private val _filterParams = combine(
    _searchQuery,
    _filterType,
    _filterCategory,
    _selectedMonthOffset
  ) { query, typeFilter, catFilter, monthOffset ->
    FilterParams(query, typeFilter, catFilter, monthOffset)
  }

  val uiState: StateFlow<FinanceUiState> = combine(
    repository.allTransactions,
    repository.allBudgets,
    _filterParams
  ) { transactions: List<TransactionEntity>, budgets: List<BudgetEntity>, params: FilterParams ->

    val query = params.query
    val typeFilter = params.typeFilter
    val catFilter = params.catFilter
    val monthOffset = params.monthOffset

    // Compute month boundaries based on offset
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, monthOffset)
    val monthName = DateUtils.formatMonthYear(cal.timeInMillis)

    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfMonth = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val endOfMonth = cal.timeInMillis

    // Month transactions
    val monthTransactions = transactions.filter {
      it.timestamp in startOfMonth..endOfMonth
    }

    // Totals for this selected month
    val monthExpenses = monthTransactions
      .filter { it.type == TransactionType.EXPENSE }
      .sumOf { it.amount }

    val monthIncome = monthTransactions
      .filter { it.type == TransactionType.INCOME }
      .sumOf { it.amount }

    val netSavings = monthIncome - monthExpenses

    // Category spend breakdown
    val categorySpend = monthTransactions
      .filter { it.type == TransactionType.EXPENSE }
      .groupBy { it.category }
      .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Automated Budget Alerts calculation
    val currentDay = if (monthOffset == 0) DateUtils.getCurrentDayOfMonth() else cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysRemaining = if (monthOffset == 0) DateUtils.getDaysRemainingInCurrentMonth() else 1

    val alerts = budgets.mapNotNull { budget ->
      if (!budget.isAlertEnabled) return@mapNotNull null

      val isOverall = budget.category.equals("Overall", ignoreCase = true)
      val spent = if (isOverall) monthExpenses else (categorySpend[budget.category] ?: 0.0)
      val limit = budget.monthlyLimit
      if (limit <= 0) return@mapNotNull null

      val percentage = (spent / limit) * 100.0
      val threshold = budget.alertThresholdPercent

      val severity = when {
        percentage >= 100.0 -> AlertSeverity.EXCEEDED
        percentage >= threshold -> AlertSeverity.WARNING
        else -> AlertSeverity.SAFE
      }

      val remaining = (limit - spent).coerceAtLeast(0.0)
      val dailyRunRate = if (currentDay > 0) spent / currentDay else 0.0
      val projectedEnd = dailyRunRate * totalDays
      val dailyCap = if (daysRemaining > 0) remaining / daysRemaining else 0.0

      val message = when (severity) {
        AlertSeverity.EXCEEDED -> {
          val overBy = spent - limit
          "Over budget by ${CurrencyUtils.formatCurrency(overBy)} (${percentage.toInt()}%)"
        }
        AlertSeverity.WARNING -> {
          "At ${percentage.toInt()}% of budget (${threshold}% alert threshold). Daily cap: ${CurrencyUtils.formatCurrency(dailyCap)}"
        }
        AlertSeverity.SAFE -> {
          "${percentage.toInt()}% used. Remaining: ${CurrencyUtils.formatCurrency(remaining)}"
        }
      }

      BudgetAlert(
        category = budget.category,
        monthlyLimit = limit,
        spentAmount = spent,
        percentageUsed = percentage,
        alertThresholdPercent = threshold,
        severity = severity,
        message = message,
        remainingBudget = remaining,
        projectedMonthEndSpend = projectedEnd,
        recommendedDailyCap = dailyCap,
        daysRemainingInMonth = daysRemaining
      )
    }

    val overallBudget = budgets.find { it.category.equals("Overall", ignoreCase = true) }
    val overallAlert = alerts.find { it.category.equals("Overall", ignoreCase = true) }
    val categoryAlerts = alerts.filter { !it.category.equals("Overall", ignoreCase = true) }
    val activeAlerts = alerts.filter { it.severity != AlertSeverity.SAFE }

    // Filtered list for Transactions tab
    val filtered = monthTransactions.filter { tx ->
      val matchesQuery = query.isBlank() ||
        tx.title.contains(query, ignoreCase = true) ||
        tx.category.contains(query, ignoreCase = true) ||
        tx.note.contains(query, ignoreCase = true)

      val matchesType = typeFilter == null || tx.type == typeFilter
      val matchesCategory = catFilter == null || tx.category.equals(catFilter, ignoreCase = true)

      matchesQuery && matchesType && matchesCategory
    }

    FinanceUiState(
      allTransactions = transactions,
      filteredTransactions = filtered,
      budgets = budgets,
      currentMonthExpenses = monthExpenses,
      currentMonthIncome = monthIncome,
      netSavings = netSavings,
      overallBudget = overallBudget,
      overallAlert = overallAlert,
      categoryAlerts = categoryAlerts,
      activeAlerts = activeAlerts,
      categorySpendMap = categorySpend,
      selectedMonthOffset = monthOffset,
      selectedMonthName = monthName,
      searchQuery = query,
      filterType = typeFilter,
      filterCategory = catFilter,
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = FinanceUiState()
  )

  fun addTransaction(
    transaction: TransactionEntity,
    context: Context? = null,
    onResult: ((Boolean, String) -> Unit)? = null
  ) {
    viewModelScope.launch {
      val generatedId = repository.insertTransaction(transaction)
      val savedTransaction = transaction.copy(id = generatedId)

      if (transaction.type == TransactionType.EXPENSE && context != null) {
        checkAndDispatchAlertNotification(context, transaction.category, transaction.amount)
      }

      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.isRealtimeSyncEnabled && syncConfig.scriptUrl.isNotBlank()) {
        val result = googleSheetsClient.storeTransactionRealtime(syncConfig.scriptUrl, savedTransaction)
        result.onSuccess { msg ->
          sheetSyncManager.recordSyncSuccess(msg)
          onResult?.invoke(true, msg)
        }.onFailure { err ->
          val errStr = "Google Sheet sync failed: ${err.message ?: "Network error"}"
          sheetSyncManager.recordSyncFailure(errStr)
          onResult?.invoke(false, errStr)
        }
      } else {
        onResult?.invoke(true, "Saved to local Room DB")
      }
    }
  }

  private suspend fun checkAndDispatchAlertNotification(
    context: Context,
    category: String,
    newExpenseAmount: Double
  ) {
    val currentState = uiState.value
    val budget = currentState.budgets.find {
      it.category.equals(category, ignoreCase = true) && it.isAlertEnabled
    } ?: return

    val currentSpent = currentState.categorySpendMap[category] ?: 0.0
    val totalSpent = currentSpent + newExpenseAmount
    val limit = budget.monthlyLimit
    if (limit <= 0) return

    val percentage = ((totalSpent / limit) * 100).toInt()
    if (percentage >= 100) {
      NotificationHelper.sendBudgetAlertNotification(
        context = context,
        category = category,
        percentage = percentage,
        spent = totalSpent,
        limit = limit,
        isExceeded = true
      )
    } else if (percentage >= budget.alertThresholdPercent) {
      NotificationHelper.sendBudgetAlertNotification(
        context = context,
        category = category,
        percentage = percentage,
        spent = totalSpent,
        limit = limit,
        isExceeded = false
      )
    }
  }

  fun updateTransaction(
    transaction: TransactionEntity,
    onResult: ((Boolean, String) -> Unit)? = null
  ) {
    viewModelScope.launch {
      repository.updateTransaction(transaction)

      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.isRealtimeSyncEnabled && syncConfig.scriptUrl.isNotBlank()) {
        val result = googleSheetsClient.storeTransactionRealtime(syncConfig.scriptUrl, transaction)
        result.onSuccess { msg ->
          sheetSyncManager.recordSyncSuccess(msg)
          onResult?.invoke(true, msg)
        }.onFailure { err ->
          val errStr = "Google Sheet sync failed: ${err.message ?: "Network error"}"
          sheetSyncManager.recordSyncFailure(errStr)
          onResult?.invoke(false, errStr)
        }
      } else {
        onResult?.invoke(true, "Updated in Room DB")
      }
    }
  }

  fun deleteTransaction(
    transaction: TransactionEntity,
    onResult: ((Boolean, String) -> Unit)? = null
  ) {
    viewModelScope.launch {
      repository.deleteTransaction(transaction)

      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.isRealtimeSyncEnabled && syncConfig.scriptUrl.isNotBlank()) {
        val result = googleSheetsClient.deleteTransactionRealtime(syncConfig.scriptUrl, transaction.id)
        result.onSuccess { msg ->
          sheetSyncManager.recordSyncSuccess(msg)
          onResult?.invoke(true, msg)
        }.onFailure { err ->
          val errStr = "Google Sheet sync failed: ${err.message ?: "Network error"}"
          sheetSyncManager.recordSyncFailure(errStr)
          onResult?.invoke(false, errStr)
        }
      } else {
        onResult?.invoke(true, "Deleted from Room DB")
      }
    }
  }

  fun addOrUpdateBudget(category: String, monthlyLimit: Double, thresholdPercent: Int) {
    viewModelScope.launch {
      val existing = uiState.value.budgets.find { it.category.equals(category, ignoreCase = true) }
      if (existing != null) {
        repository.updateBudget(
          existing.copy(
            monthlyLimit = monthlyLimit,
            alertThresholdPercent = thresholdPercent
          )
        )
      } else {
        repository.insertBudget(
          BudgetEntity(
            category = category,
            monthlyLimit = monthlyLimit,
            alertThresholdPercent = thresholdPercent,
            isAlertEnabled = true
          )
        )
      }
    }
  }

  fun deleteBudget(budget: BudgetEntity) {
    viewModelScope.launch {
      repository.deleteBudget(budget)
    }
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setFilterType(type: TransactionType?) {
    _filterType.value = type
  }

  fun setFilterCategory(category: String?) {
    _filterCategory.value = category
  }

  fun setMonthOffset(offset: Int) {
    _selectedMonthOffset.value = offset
  }

  fun resetToDemoData() {
    viewModelScope.launch {
      repository.resetToDemoData()
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
    }
  }

  fun calculateSimulatedAlert(category: String, amount: Double): BudgetAlert? {
    if (amount <= 0.0) return null
    val budget = uiState.value.budgets.find { it.category.equals(category, ignoreCase = true) } ?: return null
    val currentSpent = uiState.value.categorySpendMap[category] ?: 0.0
    val newSpent = currentSpent + amount
    val limit = budget.monthlyLimit
    if (limit <= 0) return null

    val percentage = (newSpent / limit) * 100.0
    val threshold = budget.alertThresholdPercent

    if (percentage < threshold) return null

    val severity = if (percentage >= 100.0) AlertSeverity.EXCEEDED else AlertSeverity.WARNING
    val remaining = (limit - newSpent).coerceAtLeast(0.0)
    val days = DateUtils.getDaysRemainingInCurrentMonth()

    val message = if (severity == AlertSeverity.EXCEEDED) {
      "⚠️ Adding this expense will exceed budget by ${CurrencyUtils.formatCurrency(newSpent - limit)} (${percentage.toInt()}%)"
    } else {
      "⚠️ Will reach ${percentage.toInt()}% of budget (${threshold}% alert limit)"
    }

    return BudgetAlert(
      category = category,
      monthlyLimit = limit,
      spentAmount = newSpent,
      percentageUsed = percentage,
      alertThresholdPercent = threshold,
      severity = severity,
      message = message,
      remainingBudget = remaining,
      projectedMonthEndSpend = 0.0,
      recommendedDailyCap = if (days > 0) remaining / days else 0.0,
      daysRemainingInMonth = days
    )
  }

  // --- Google Sheet API & Real-time Sync Methods ---

  fun updateGoogleSheetUrl(url: String) {
    sheetSyncManager.updateScriptUrl(url)
  }

  fun toggleGoogleSheetAutoSync(enabled: Boolean) {
    sheetSyncManager.setRealtimeSyncEnabled(enabled)
  }

  fun testGoogleSheetConnection(url: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      sheetSyncManager.setSyncing(true)
      val result = googleSheetsClient.testConnection(url)
      sheetSyncManager.setSyncing(false)
      result.onSuccess { msg ->
        sheetSyncManager.recordSyncSuccess(msg)
        onResult(true, msg)
      }.onFailure { err ->
        val errorMsg = err.message ?: "Connection failed"
        sheetSyncManager.recordSyncFailure(errorMsg)
        onResult(false, errorMsg)
      }
    }
  }

  fun syncAllToGoogleSheet(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
    viewModelScope.launch {
      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.scriptUrl.isBlank()) {
        onComplete(false, "Please configure your Google Apps Script Web App URL first.")
        return@launch
      }

      sheetSyncManager.setSyncing(true)
      val snapshot = repository.getAllTransactionsSnapshot()
      val result = googleSheetsClient.syncAllTransactions(syncConfig.scriptUrl, snapshot)
      result.onSuccess { msg ->
        sheetSyncManager.recordSyncSuccess(msg)
        onComplete(true, msg)
      }.onFailure { err ->
        val errMsg = err.message ?: "Failed to sync transactions"
        sheetSyncManager.recordSyncFailure(errMsg)
        onComplete(false, errMsg)
      }
    }
  }

  fun pullFromGoogleSheet(onComplete: (Boolean, String, Int) -> Unit = { _, _, _ -> }) {
    viewModelScope.launch {
      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.scriptUrl.isBlank()) {
        onComplete(false, "Please configure your Google Apps Script Web App URL first.", 0)
        return@launch
      }

      sheetSyncManager.setSyncing(true)
      val result = googleSheetsClient.fetchRemoteTransactions(syncConfig.scriptUrl)
      result.onSuccess { remoteList ->
        if (remoteList.isNotEmpty()) {
          repository.insertTransactions(remoteList)
        }
        val msg = "Imported ${remoteList.size} transactions from Google Sheet"
        sheetSyncManager.recordSyncSuccess(msg)
        onComplete(true, msg, remoteList.size)
      }.onFailure { err ->
        val errMsg = err.message ?: "Failed to fetch from Google Sheet"
        sheetSyncManager.recordSyncFailure(errMsg)
        onComplete(false, errMsg, 0)
      }
    }
  }

  fun sendTestTransactionToSheet(onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val syncConfig = sheetSyncManager.syncState.value
      if (syncConfig.scriptUrl.isBlank()) {
        onResult(false, "Google Apps Script URL is empty")
        return@launch
      }

      sheetSyncManager.setSyncing(true)
      val testTx = TransactionEntity(
        id = System.currentTimeMillis() % 100000,
        title = "Test Real-time Sync",
        amount = 12.34,
        type = TransactionType.EXPENSE,
        category = "Utilities",
        paymentMethod = "Online",
        timestamp = System.currentTimeMillis(),
        note = "Verified via Android Room & Google Sheet Script API"
      )

      val result = googleSheetsClient.storeTransactionRealtime(syncConfig.scriptUrl, testTx)
      result.onSuccess { msg ->
        sheetSyncManager.recordSyncSuccess(msg)
        onResult(true, "Successfully sent real-time test row to Google Sheet!")
      }.onFailure { err ->
        val errMsg = err.message ?: "Failed to send test transaction"
        sheetSyncManager.recordSyncFailure(errMsg)
        onResult(false, errMsg)
      }
    }
  }
}
