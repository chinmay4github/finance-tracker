package com.example.data.remote

import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GoogleSheetsApiClient {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private fun parseHttpError(code: Int, message: String, bodyString: String): String {
    if (code == 403 || code == 401 ||
      bodyString.contains("需要存取權") ||
      bodyString.contains("Need Permission") ||
      bodyString.contains("accounts.google.com") ||
      bodyString.contains("drive-logo") ||
      bodyString.contains("Sign in - Google Accounts")
    ) {
      return "HTTP 403: Apps Script requires 'Who has access' set to 'Anyone' (not 'Only myself'). In Apps Script, go to Deploy > Manage deployments > Edit > set 'Who has access: Anyone' > Deploy."
    }
    return "HTTP $code: $message"
  }

  private fun isPermissionErrorHtml(bodyString: String): Boolean {
    return bodyString.contains("需要存取權") ||
      bodyString.contains("Need Permission") ||
      bodyString.contains("accounts.google.com") ||
      bodyString.contains("drive-logo") ||
      (bodyString.contains("<html", ignoreCase = true) && bodyString.contains("Google", ignoreCase = true))
  }

  /**
   * Tests the connection to the Google Apps Script Web API endpoint.
   */
  suspend fun testConnection(scriptUrl: String): Result<String> = withContext(Dispatchers.IO) {
    if (scriptUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Script URL is empty"))
    }

    try {
      val testUrl = if (scriptUrl.contains("?")) "$scriptUrl&action=ping" else "$scriptUrl?action=ping"
      val request = Request.Builder()
        .url(testUrl)
        .get()
        .header("Accept", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string().orEmpty()
        if (isPermissionErrorHtml(bodyString)) {
          return@withContext Result.failure(
            IOException("HTTP 403: Google Apps Script requires 'Who has access' set to 'Anyone'. In Apps Script, open Deploy > Manage deployments > Edit > set 'Who has access: Anyone' > Deploy.")
          )
        }

        if (response.isSuccessful) {
          try {
            val json = JSONObject(bodyString)
            val status = json.optString("status")
            val msg = json.optString("message", "Connected successfully")
            val sheetName = json.optString("spreadsheetName", "")
            if (status == "success" || status == "ok") {
              val detail = if (sheetName.isNotBlank()) "$msg ($sheetName)" else msg
              Result.success(detail)
            } else {
              Result.failure(IOException(json.optString("message", "Unknown script error")))
            }
          } catch (e: Exception) {
            if (bodyString.contains("online", ignoreCase = true) || response.code == 200) {
              Result.success("Connected to Google Sheets API")
            } else {
              Result.failure(IOException("Invalid response: ${bodyString.take(100)}"))
            }
          }
        } else {
          Result.failure(IOException(parseHttpError(response.code, response.message, bodyString)))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Stores or updates a transaction in real-time in Google Sheets.
   */
  suspend fun storeTransactionRealtime(
    scriptUrl: String,
    transaction: TransactionEntity
  ): Result<String> = withContext(Dispatchers.IO) {
    if (scriptUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Script URL not configured"))
    }

    try {
      val txJson = JSONObject().apply {
        put("id", transaction.id)
        put("title", transaction.title)
        put("amount", transaction.amount)
        put("type", transaction.type.name)
        put("category", transaction.category)
        put("paymentMethod", transaction.paymentMethod)
        put("timestamp", transaction.timestamp)
        put("dateFormatted", DateUtils.formatFullDate(transaction.timestamp))
        put("note", transaction.note)
      }

      val payload = JSONObject().apply {
        put("action", "STORE_TRANSACTION")
        put("transaction", txJson)
      }

      val body = payload.toString().toRequestBody(jsonMediaType)
      val request = Request.Builder()
        .url(scriptUrl)
        .post(body)
        .header("Content-Type", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string().orEmpty()
        if (isPermissionErrorHtml(bodyString)) {
          return@withContext Result.failure(
            IOException("HTTP 403: Google Apps Script requires 'Who has access' set to 'Anyone'. In Apps Script, open Deploy > Manage deployments > Edit > set 'Who has access: Anyone' > Deploy.")
          )
        }

        if (response.isSuccessful) {
          try {
            val json = JSONObject(bodyString)
            val status = json.optString("status")
            if (status == "success" || status == "ok") {
              Result.success(json.optString("message", "Saved to Google Sheet in real time"))
            } else {
              Result.failure(IOException(json.optString("message", "Failed to store in Google Sheet")))
            }
          } catch (e: Exception) {
            Result.success("Transaction recorded in Google Sheet")
          }
        } else {
          Result.failure(IOException(parseHttpError(response.code, response.message, bodyString)))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Deletes a transaction from Google Sheets by ID in real-time.
   */
  suspend fun deleteTransactionRealtime(
    scriptUrl: String,
    transactionId: Long
  ): Result<String> = withContext(Dispatchers.IO) {
    if (scriptUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Script URL not configured"))
    }

    try {
      val payload = JSONObject().apply {
        put("action", "DELETE_TRANSACTION")
        put("id", transactionId)
      }

      val body = payload.toString().toRequestBody(jsonMediaType)
      val request = Request.Builder()
        .url(scriptUrl)
        .post(body)
        .header("Content-Type", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string().orEmpty()
        if (isPermissionErrorHtml(bodyString)) {
          return@withContext Result.failure(
            IOException("HTTP 403: Google Apps Script requires 'Who has access' set to 'Anyone'.")
          )
        }

        if (response.isSuccessful) {
          Result.success("Deleted from Google Sheet")
        } else {
          Result.failure(IOException(parseHttpError(response.code, response.message, bodyString)))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Pushes all local Room DB transactions to Google Sheet in batch.
   */
  suspend fun syncAllTransactions(
    scriptUrl: String,
    transactions: List<TransactionEntity>
  ): Result<String> = withContext(Dispatchers.IO) {
    if (scriptUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Script URL not configured"))
    }

    try {
      val txArray = JSONArray()
      for (tx in transactions) {
        val obj = JSONObject().apply {
          put("id", tx.id)
          put("title", tx.title)
          put("amount", tx.amount)
          put("type", tx.type.name)
          put("category", tx.category)
          put("paymentMethod", tx.paymentMethod)
          put("timestamp", tx.timestamp)
          put("dateFormatted", DateUtils.formatFullDate(tx.timestamp))
          put("note", tx.note)
        }
        txArray.put(obj)
      }

      val payload = JSONObject().apply {
        put("action", "SYNC_ALL")
        put("transactions", txArray)
      }

      val body = payload.toString().toRequestBody(jsonMediaType)
      val request = Request.Builder()
        .url(scriptUrl)
        .post(body)
        .header("Content-Type", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string().orEmpty()
        if (isPermissionErrorHtml(bodyString)) {
          return@withContext Result.failure(
            IOException("HTTP 403: Google Apps Script requires 'Who has access' set to 'Anyone'.")
          )
        }

        if (response.isSuccessful) {
          Result.success("Synced ${transactions.size} transactions to Google Sheet")
        } else {
          Result.failure(IOException(parseHttpError(response.code, response.message, bodyString)))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches remote transactions from Google Sheet.
   */
  suspend fun fetchRemoteTransactions(
    scriptUrl: String
  ): Result<List<TransactionEntity>> = withContext(Dispatchers.IO) {
    if (scriptUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Script URL not configured"))
    }

    try {
      val url = if (scriptUrl.contains("?")) "$scriptUrl&action=fetch" else "$scriptUrl?action=fetch"
      val request = Request.Builder()
        .url(url)
        .get()
        .header("Accept", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string().orEmpty()
        if (isPermissionErrorHtml(bodyString)) {
          return@withContext Result.failure(
            IOException("HTTP 403: Google Apps Script requires 'Who has access' set to 'Anyone'.")
          )
        }

        if (!response.isSuccessful) {
          return@withContext Result.failure(IOException(parseHttpError(response.code, response.message, bodyString)))
        }

        val json = JSONObject(bodyString)
        val transactionsArray = json.optJSONArray("transactions") ?: JSONArray()
        val list = mutableListOf<TransactionEntity>()

        for (i in 0 until transactionsArray.length()) {
          val item = transactionsArray.getJSONObject(i)
          val id = item.optLong("id", System.currentTimeMillis() + i)
          val title = item.optString("title", "Untitled")
          val amount = item.optDouble("amount", 0.0)
          val typeStr = item.optString("type", "EXPENSE")
          val type = if (typeStr.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
          val category = item.optString("category", "General")
          val paymentMethod = item.optString("paymentMethod", "Card")
          val timestamp = item.optLong("timestamp", System.currentTimeMillis())
          val note = item.optString("note", "")

          list.add(
            TransactionEntity(
              id = if (id > 0) id else 0,
              title = title,
              amount = amount,
              type = type,
              category = category,
              paymentMethod = paymentMethod,
              timestamp = timestamp,
              note = note
            )
          )
        }

        Result.success(list)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
