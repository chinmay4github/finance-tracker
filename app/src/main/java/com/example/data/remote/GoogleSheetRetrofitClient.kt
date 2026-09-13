package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.TransactionEntity
import com.example.data.remote.model.GoogleSheetSyncRequest
import com.example.data.remote.model.GoogleSheetSyncResponse
import com.example.data.remote.model.toGoogleSheetDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Singleton client managing the Retrofit instance and HTTP communication
 * with the Google Apps Script Web App defined in the environment variables.
 */
object GoogleSheetRetrofitClient {

  /**
   * The default Web App URL injected from environment variables (`.env` / BuildConfig).
   */
  val defaultEnvironmentUrl: String = BuildConfig.GOOGLE_SHEET_WEB_APP_URL

  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .addInterceptor(
      HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
      }
    )
    .build()

  /**
   * Fallback base URL for Retrofit initialization.
   * Actual requests can target dynamic endpoints via @Url or the default URL from environment variables.
   */
  private const val BASE_URL = "https://script.google.com/macros/s/"

  val apiService: GoogleSheetApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(GoogleSheetApiService::class.java)
  }

  /**
   * Pings the Google Apps Script endpoint to test connectivity.
   */
  suspend fun ping(targetUrl: String = defaultEnvironmentUrl): Result<String> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Web App URL is not configured"))
    }
    try {
      val response = apiService.ping(url = targetUrl)
      handleResponse(response) { body ->
        val sheetName = body.spreadsheetName?.takeIf { it.isNotBlank() }
        val msg = body.message ?: "Connected to Google Sheets API"
        if (sheetName != null) "$msg ($sheetName)" else msg
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Stores or updates a transaction in real-time in Google Sheets via Retrofit.
   */
  suspend fun storeTransaction(
    transaction: TransactionEntity,
    targetUrl: String = defaultEnvironmentUrl
  ): Result<String> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Web App URL is not configured"))
    }
    try {
      val request = GoogleSheetSyncRequest(
        action = "STORE_TRANSACTION",
        transaction = transaction.toGoogleSheetDto()
      )
      val response = apiService.syncData(url = targetUrl, request = request)
      handleResponse(response) { body ->
        body.message ?: "Transaction saved to Google Sheet in real-time"
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Deletes a transaction from Google Sheets by ID via Retrofit.
   */
  suspend fun deleteTransaction(
    transactionId: Long,
    targetUrl: String = defaultEnvironmentUrl
  ): Result<String> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Web App URL is not configured"))
    }
    try {
      val request = GoogleSheetSyncRequest(
        action = "DELETE_TRANSACTION",
        id = transactionId
      )
      val response = apiService.syncData(url = targetUrl, request = request)
      handleResponse(response) { body ->
        body.message ?: "Transaction deleted from Google Sheet"
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Syncs all transactions to Google Sheets in batch via Retrofit.
   */
  suspend fun syncAllTransactions(
    transactions: List<TransactionEntity>,
    targetUrl: String = defaultEnvironmentUrl
  ): Result<String> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Web App URL is not configured"))
    }
    try {
      val request = GoogleSheetSyncRequest(
        action = "SYNC_ALL",
        transactions = transactions.map { it.toGoogleSheetDto() }
      )
      val response = apiService.syncData(url = targetUrl, request = request)
      handleResponse(response) { body ->
        body.message ?: "Synced ${transactions.size} transactions to Google Sheet"
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches all transactions from Google Sheet via Retrofit.
   */
  suspend fun fetchAllTransactions(
    targetUrl: String = defaultEnvironmentUrl
  ): Result<List<TransactionEntity>> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Google Sheet Web App URL is not configured"))
    }
    try {
      val response = apiService.fetchTransactions(url = targetUrl)
      handleResponse(response) { body ->
        body.transactions?.map { it.toTransactionEntity() } ?: emptyList()
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun <T, R> handleResponse(response: Response<T>, transform: (T) -> R): Result<R> {
    if (response.isSuccessful) {
      val body = response.body()
      return if (body != null) {
        if (body is GoogleSheetSyncResponse && body.status == "error") {
          Result.failure(IOException(body.message ?: "Remote script error"))
        } else {
          Result.success(transform(body))
        }
      } else {
        Result.failure(IOException("Empty response body from Google Sheets"))
      }
    } else {
      val code = response.code()
      val errorMsg = when (code) {
        401, 403 -> "HTTP $code: Permission denied. Ensure 'Who has access' is set to 'Anyone' in Apps Script deployment."
        404 -> "HTTP 404: Google Apps Script Web App URL not found."
        500 -> "HTTP 500: Google Apps Script execution error."
        else -> "HTTP $code: ${response.message()}"
      }
      return Result.failure(IOException(errorMsg))
    }
  }
}
