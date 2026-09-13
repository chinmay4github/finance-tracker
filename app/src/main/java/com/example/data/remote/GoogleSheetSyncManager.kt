package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GoogleSheetSyncState(
  val scriptUrl: String = "",
  val isRealtimeSyncEnabled: Boolean = false,
  val isSyncing: Boolean = false,
  val lastSyncTimestamp: Long = 0L,
  val lastSyncStatus: String = "Not configured",
  val isConnected: Boolean = false
)

class GoogleSheetSyncManager(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("google_sheets_sync_prefs", Context.MODE_PRIVATE)

  private val _syncState = MutableStateFlow(loadInitialState())
  val syncState: StateFlow<GoogleSheetSyncState> = _syncState.asStateFlow()

  private fun loadInitialState(): GoogleSheetSyncState {
    val defaultUrl = GoogleSheetScriptTemplate.DEFAULT_DEMO_URL
    val savedUrl = prefs.getString(KEY_SCRIPT_URL, null)
    val url = (savedUrl?.takeIf { it.isNotBlank() } ?: defaultUrl).trim()
    val autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true)
    val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
    val isConnected = url.isNotBlank()
    val lastStatus = prefs.getString(
      KEY_LAST_STATUS,
      if (isConnected) "Connected & Ready" else "Not connected"
    ) ?: if (isConnected) "Connected & Ready" else "Not connected"

    return GoogleSheetSyncState(
      scriptUrl = url,
      isRealtimeSyncEnabled = autoSync,
      isSyncing = false,
      lastSyncTimestamp = lastSync,
      lastSyncStatus = lastStatus,
      isConnected = isConnected
    )
  }

  fun updateScriptUrl(url: String) {
    val cleanUrl = url.trim()
    prefs.edit().putString(KEY_SCRIPT_URL, cleanUrl).apply()
    val isConnected = cleanUrl.isNotBlank()
    prefs.edit().putBoolean(KEY_IS_CONNECTED, isConnected).apply()

    _syncState.value = _syncState.value.copy(
      scriptUrl = cleanUrl,
      isConnected = isConnected,
      lastSyncStatus = if (isConnected) "Script configured" else "Not connected"
    )
  }

  fun setRealtimeSyncEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    _syncState.value = _syncState.value.copy(isRealtimeSyncEnabled = enabled)
  }

  fun setSyncing(isSyncing: Boolean) {
    _syncState.value = _syncState.value.copy(isSyncing = isSyncing)
  }

  fun recordSyncSuccess(message: String) {
    val now = System.currentTimeMillis()
    prefs.edit()
      .putLong(KEY_LAST_SYNC, now)
      .putString(KEY_LAST_STATUS, message)
      .putBoolean(KEY_IS_CONNECTED, true)
      .apply()

    _syncState.value = _syncState.value.copy(
      isSyncing = false,
      lastSyncTimestamp = now,
      lastSyncStatus = message,
      isConnected = true
    )
  }

  fun recordSyncFailure(errorMsg: String) {
    prefs.edit().putString(KEY_LAST_STATUS, errorMsg).apply()
    _syncState.value = _syncState.value.copy(
      isSyncing = false,
      lastSyncStatus = errorMsg
    )
  }

  companion object {
    private const val KEY_SCRIPT_URL = "key_google_sheet_script_url"
    private const val KEY_AUTO_SYNC = "key_google_sheet_auto_sync"
    private const val KEY_LAST_SYNC = "key_google_sheet_last_sync_timestamp"
    private const val KEY_LAST_STATUS = "key_google_sheet_last_status"
    private const val KEY_IS_CONNECTED = "key_google_sheet_is_connected"
  }
}
