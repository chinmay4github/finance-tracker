package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddEditBudgetDialog
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.GoogleSheetSyncModal
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.AlertDangerRed
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

sealed class NavigationTab(val index: Int, val title: String, val icon: ImageVector, val tag: String) {
  object Overview : NavigationTab(0, "Overview", Icons.Default.Dashboard, "tab_overview")
  object Transactions : NavigationTab(1, "Transactions", Icons.Default.ReceiptLong, "tab_transactions")
  object Budgets : NavigationTab(2, "Budgets", Icons.Default.NotificationsActive, "tab_budgets")
  object Analytics : NavigationTab(3, "Analytics", Icons.Default.Insights, "tab_analytics")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FinanceApp()
      }
    }
  }
}

@Composable
fun FinanceApp(viewModel: FinanceViewModel = viewModel()) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val sheetSyncState by viewModel.sheetSyncState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var selectedTab by remember { mutableIntStateOf(0) }

  // Modals state
  var isAddTransactionOpen by remember { mutableStateOf(false) }
  var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
  var isAddBudgetOpen by remember { mutableStateOf(false) }
  var budgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }
  var isGoogleSheetSyncOpen by remember { mutableStateOf(false) }

  // Request notification permission on Android 13+ (graceful check)
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Handled gracefully */ }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        val tabs = listOf(
          NavigationTab.Overview,
          NavigationTab.Transactions,
          NavigationTab.Budgets,
          NavigationTab.Analytics
        )

        tabs.forEach { tab ->
          val isSelected = selectedTab == tab.index

          NavigationBarItem(
            selected = isSelected,
            onClick = { selectedTab = tab.index },
            icon = {
              if (tab == NavigationTab.Budgets && uiState.activeAlerts.isNotEmpty()) {
                BadgedBox(
                  badge = {
                    Badge(
                      containerColor = AlertDangerRed,
                      contentColor = Color.White
                    ) {
                      Text(uiState.activeAlerts.size.toString())
                    }
                  }
                ) {
                  Icon(tab.icon, contentDescription = tab.title)
                }
              } else {
                Icon(tab.icon, contentDescription = tab.title)
              }
            },
            label = { Text(tab.title) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag(tab.tag)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> DashboardScreen(
          uiState = uiState,
          syncState = sheetSyncState,
          onOpenSheetSync = { isGoogleSheetSyncOpen = true },
          onAddTransactionClicked = { isAddTransactionOpen = true },
          onViewAllTransactionsClicked = { selectedTab = 1 },
          onViewBudgetsClicked = { selectedTab = 2 },
          onTransactionClicked = { tx -> transactionToEdit = tx }
        )
        1 -> TransactionsScreen(
          uiState = uiState,
          syncState = sheetSyncState,
          onOpenSheetSync = { isGoogleSheetSyncOpen = true },
          onSearchChanged = { viewModel.setSearchQuery(it) },
          onFilterTypeChanged = { viewModel.setFilterType(it) },
          onFilterCategoryChanged = { viewModel.setFilterCategory(it) },
          onMonthOffsetChanged = { viewModel.setMonthOffset(it) },
          onAddTransactionClicked = { isAddTransactionOpen = true },
          onEditTransactionClicked = { tx -> transactionToEdit = tx },
          onDeleteTransactionClicked = { tx ->
            viewModel.deleteTransaction(tx) { success, msg ->
              scope.launch {
                val feedback = if (success && sheetSyncState.isRealtimeSyncEnabled && sheetSyncState.scriptUrl.isNotBlank()) {
                  "Deleted '${tx.title}' (Synced to Google Sheet)"
                } else {
                  "Deleted '${tx.title}' from Room DB"
                }
                snackbarHostState.showSnackbar(feedback)
              }
            }
          }
        )
        2 -> BudgetsScreen(
          uiState = uiState,
          onAddBudgetClicked = { isAddBudgetOpen = true },
          onEditBudgetClicked = { budget -> budgetToEdit = budget },
          onResetDemoData = {
            viewModel.resetToDemoData()
            scope.launch {
              snackbarHostState.showSnackbar("Sample data reset")
            }
          }
        )
        3 -> AnalyticsScreen(
          uiState = uiState
        )
      }
    }

    // Add / Edit Transaction Bottom Sheet
    if (isAddTransactionOpen || transactionToEdit != null) {
      AddEditTransactionSheet(
        initialTransaction = transactionToEdit,
        onDismiss = {
          isAddTransactionOpen = false
          transactionToEdit = null
        },
        onSave = { tx ->
          if (transactionToEdit != null) {
            viewModel.updateTransaction(tx) { success, msg ->
              scope.launch {
                val feedback = if (success && sheetSyncState.isRealtimeSyncEnabled && sheetSyncState.scriptUrl.isNotBlank()) {
                  "Updated '${tx.title}' • Synced to Google Sheet"
                } else {
                  "Updated '${tx.title}' in Room DB"
                }
                snackbarHostState.showSnackbar(feedback)
              }
            }
          } else {
            viewModel.addTransaction(tx, context) { success, msg ->
              scope.launch {
                val feedback = if (success && sheetSyncState.isRealtimeSyncEnabled && sheetSyncState.scriptUrl.isNotBlank()) {
                  "Saved '${tx.title}' • Stored in Room & Google Sheet!"
                } else {
                  "Saved '${tx.title}' to Room DB"
                }
                snackbarHostState.showSnackbar(feedback)
              }
            }
          }
          isAddTransactionOpen = false
          transactionToEdit = null
        },
        onSimulateAlert = { cat, amt ->
          viewModel.calculateSimulatedAlert(cat, amt)
        }
      )
    }

    // Add Budget Dialog
    if (isAddBudgetOpen) {
      AddEditBudgetDialog(
        initialBudget = null,
        onDismiss = { isAddBudgetOpen = false },
        onSave = { category, limit, threshold ->
          viewModel.addOrUpdateBudget(category, limit, threshold)
          scope.launch { snackbarHostState.showSnackbar("Budget set for $category") }
          isAddBudgetOpen = false
        }
      )
    }

    // Edit Budget Dialog
    budgetToEdit?.let { budget ->
      AddEditBudgetDialog(
        initialBudget = budget,
        onDismiss = { budgetToEdit = null },
        onSave = { category, limit, threshold ->
          viewModel.addOrUpdateBudget(category, limit, threshold)
          scope.launch { snackbarHostState.showSnackbar("Updated budget for $category") }
          budgetToEdit = null
        },
        onDelete = {
          viewModel.deleteBudget(it)
          scope.launch { snackbarHostState.showSnackbar("Deleted budget for ${it.category}") }
          budgetToEdit = null
        }
      )
    }

    // Google Sheets Cloud API Real-Time Sync Modal
    if (isGoogleSheetSyncOpen) {
      GoogleSheetSyncModal(
        syncState = sheetSyncState,
        onDismiss = { isGoogleSheetSyncOpen = false },
        onUpdateUrl = { viewModel.updateGoogleSheetUrl(it) },
        onToggleAutoSync = { viewModel.toggleGoogleSheetAutoSync(it) },
        onTestConnection = { url, cb -> viewModel.testGoogleSheetConnection(url, cb) },
        onSyncAll = { cb -> viewModel.syncAllToGoogleSheet(cb) },
        onPullFromSheet = { cb -> viewModel.pullFromGoogleSheet(cb) },
        onSendTestTransaction = { cb -> viewModel.sendTestTransactionToSheet(cb) }
      )
    }
  }
}

// Retained for Screenshot & Preview tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun FinanceAppPreview() {
  MyApplicationTheme {
    Greeting("Finance Tracker")
  }
}
