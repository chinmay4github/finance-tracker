package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GoogleSheetScriptTemplate
import com.example.data.remote.GoogleSheetSyncState
import com.example.util.DateUtils
import kotlinx.coroutines.launch

private val GoogleSheetsGreen = Color(0xFF0F9D58)
private val GoogleSheetsGreenLight = Color(0xFFE6F4EA)
private val GoogleSheetsGreenDark = Color(0xFF0B8043)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSheetSyncModal(
  syncState: GoogleSheetSyncState,
  onDismiss: () -> Unit,
  onUpdateUrl: (String) -> Unit,
  onToggleAutoSync: (Boolean) -> Unit,
  onTestConnection: (String, (Boolean, String) -> Unit) -> Unit,
  onSyncAll: ((Boolean, String) -> Unit) -> Unit,
  onPullFromSheet: ((Boolean, String, Int) -> Unit) -> Unit,
  onSendTestTransaction: ((Boolean, String) -> Unit) -> Unit
) {
  val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  var inputUrl by remember(syncState.scriptUrl) { mutableStateOf(syncState.scriptUrl) }
  var isTestingConnection by remember { mutableStateOf(false) }
  var isSyncingAll by remember { mutableStateOf(false) }
  var isPullingData by remember { mutableStateOf(false) }
  var isSendingTestRow by remember { mutableStateOf(false) }
  var feedbackMessage by remember { mutableStateOf<String?>(null) }
  var isFeedbackSuccess by remember { mutableStateOf(true) }
  var isScriptGuideExpanded by remember { mutableStateOf(true) }
  val userEmail = "chinmay.moharana99@gmail.com"

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("google_sheet_sync_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GoogleSheetsGreenLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = "Google Sheets",
            tint = GoogleSheetsGreen,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Google Sheets Real-Time API",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Sync Room Database with Google Sheets Script",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Live Status Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (syncState.isConnected) GoogleSheetsGreenLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (syncState.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
            contentDescription = null,
            tint = if (syncState.isConnected) GoogleSheetsGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (syncState.isConnected) "API Connected & Ready" else "Google Sheet API Not Linked",
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.bodyMedium,
              color = if (syncState.isConnected) GoogleSheetsGreenDark else MaterialTheme.colorScheme.onSurface
            )

            val statusText = if (syncState.lastSyncTimestamp > 0) {
              "Last synced: ${DateUtils.formatFullDate(syncState.lastSyncTimestamp)}"
            } else {
              syncState.lastSyncStatus
            }

            Text(
              text = statusText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (syncState.isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.5.dp,
              color = GoogleSheetsGreen
            )
          }
        }
      }

      // Feedback banner if recent operation finished
      feedbackMessage?.let { msg ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isFeedbackSuccess) GoogleSheetsGreenLight else MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isFeedbackSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = if (isFeedbackSuccess) GoogleSheetsGreenDark else MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = msg,
              style = MaterialTheme.typography.bodySmall,
              color = if (isFeedbackSuccess) GoogleSheetsGreenDark else MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }

      // Endpoint URL Input
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Google Apps Script Web App URL",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
          value = inputUrl,
          onValueChange = {
            inputUrl = it
            onUpdateUrl(it)
          },
          placeholder = { Text("https://script.google.com/macros/s/.../exec") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("google_sheet_url_input"),
          shape = RoundedCornerShape(12.dp),
          trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    inputUrl = clip.trim()
                    onUpdateUrl(inputUrl)
                    Toast.makeText(context, "Pasted URL from clipboard", Toast.LENGTH_SHORT).show()
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste URL",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              isTestingConnection = true
              feedbackMessage = null
              onTestConnection(inputUrl) { success, resultMsg ->
                isTestingConnection = false
                isFeedbackSuccess = success
                feedbackMessage = resultMsg
              }
            },
            enabled = inputUrl.isNotBlank() && !isTestingConnection,
            modifier = Modifier
              .weight(1f)
              .testTag("btn_test_sheet_connection"),
            colors = ButtonDefaults.buttonColors(containerColor = GoogleSheetsGreenDark),
            shape = RoundedCornerShape(10.dp)
          ) {
            if (isTestingConnection) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Testing...")
            } else {
              Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Test Connection")
            }
          }

          OutlinedButton(
            onClick = {
              isSendingTestRow = true
              feedbackMessage = null
              onSendTestTransaction { success, msg ->
                isSendingTestRow = false
                isFeedbackSuccess = success
                feedbackMessage = msg
              }
            },
            enabled = inputUrl.isNotBlank() && !isSendingTestRow,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("btn_send_test_row")
          ) {
            if (isSendingTestRow) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Test Row")
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Real-Time Auto Sync Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Real-Time Auto-Sync",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Automatically push new, edited, and deleted transactions to Google Sheet instantly in the background.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
          checked = syncState.isRealtimeSyncEnabled,
          onCheckedChange = { onToggleAutoSync(it) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = GoogleSheetsGreen
          ),
          modifier = Modifier.testTag("switch_realtime_sync")
        )
      }

      // Two-Way Sync Actions
      Text(
        text = "Database Sync Actions",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            isSyncingAll = true
            feedbackMessage = null
            onSyncAll { success, msg ->
              isSyncingAll = false
              isFeedbackSuccess = success
              feedbackMessage = msg
            }
          },
          enabled = inputUrl.isNotBlank() && !isSyncingAll,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_sync_all_to_sheet"),
          shape = RoundedCornerShape(12.dp)
        ) {
          if (isSyncingAll) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Push to Sheet")
          }
        }

        OutlinedButton(
          onClick = {
            isPullingData = true
            feedbackMessage = null
            onPullFromSheet { success, msg, _ ->
              isPullingData = false
              isFeedbackSuccess = success
              feedbackMessage = msg
            }
          },
          enabled = inputUrl.isNotBlank() && !isPullingData,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_pull_from_sheet"),
          shape = RoundedCornerShape(12.dp)
        ) {
          if (isPullingData) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pull to Room")
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Google Apps Script Setup Accordion
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Code,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Google Apps Script Setup (1-Click)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = { isScriptGuideExpanded = !isScriptGuideExpanded }
            ) {
              Icon(
                imageVector = if (isScriptGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand Guide"
              )
            }
          }

          AnimatedVisibility(visible = isScriptGuideExpanded) {
            Column(
              modifier = Modifier.padding(top = 10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoogleSheetsGreenLight,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Configured for: ",
                    fontSize = 11.sp,
                    color = GoogleSheetsGreenDark
                  )
                  Text(
                    text = userEmail,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoogleSheetsGreenDark
                  )
                }
              }

              // Warning / Solution for HTTP 403 Forbidden
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF7E0),
                border = BorderStroke(1.dp, Color(0xFFF9AB00)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    text = "⚠️ Getting HTTP 403 Forbidden?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFB06000)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "This happens when Google Apps Script is set to 'Only myself'.\n" +
                      "To fix it in 20 seconds:\n" +
                      "1. In Apps Script, click Deploy > Manage deployments.\n" +
                      "2. Click the ✏️ Edit icon on your deployment.\n" +
                      "3. Change 'Who has access' to: Anyone\n" +
                      "4. Click Deploy (the URL remains the same)!",
                    fontSize = 11.sp,
                    color = Color(0xFF704000),
                    lineHeight = 16.sp
                  )
                }
              }

              Text(
                text = "Step 1: Sign in with $userEmail at sheets.new (create or open a sheet).\n" +
                  "Step 2: In the top menu, click Extensions > Apps Script.\n" +
                  "Step 3: Delete any starter code in Code.gs, tap the copy button below, paste it, and click Save (💾).\n" +
                  "Step 4: Click 'Deploy' (top-right) > 'New deployment'.\n" +
                  "Step 5: Click gear icon > Select 'Web app':\n" +
                  "   • Execute as: Me ($userEmail)\n" +
                  "   • Who has access: Anyone\n" +
                  "Step 6: Click Deploy, allow access with $userEmail, copy the Web App URL, and paste it into the field above!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
              )

              Button(
                onClick = {
                  clipboardManager.setText(AnnotatedString(GoogleSheetScriptTemplate.APPS_SCRIPT_CODE))
                  Toast.makeText(context, "Google Apps Script code copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_copy_script")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Ready-to-Run Apps Script")
              }

              // Monospace script code preview
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 140.dp)
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
              ) {
                Box(
                  modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
                ) {
                  SelectionContainer {
                    Text(
                      text = GoogleSheetScriptTemplate.APPS_SCRIPT_CODE,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                      ),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
