package com.example.data.remote

import com.example.data.remote.model.GoogleSheetSyncRequest
import com.example.data.remote.model.GoogleSheetSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit interface defining HTTP communication endpoints with the
 * Google Apps Script Web App for data synchronization.
 *
 * Supports both dynamic `@Url` parameters and predefined paths,
 * allowing requests to the Web App URL configured in environment variables.
 */
interface GoogleSheetApiService {

  /**
   * Health check / Ping endpoint to verify connectivity with Google Apps Script.
   *
   * @param url The Google Apps Script Web App endpoint URL.
   * @param action The query parameter action, defaults to "ping".
   */
  @GET
  suspend fun ping(
    @Url url: String,
    @Query("action") action: String = "ping"
  ): Response<GoogleSheetSyncResponse>

  /**
   * Fetches all transactions stored in the Google Spreadsheet.
   *
   * @param url The Google Apps Script Web App endpoint URL.
   * @param action The query parameter action, defaults to "fetch".
   */
  @GET
  suspend fun fetchTransactions(
    @Url url: String,
    @Query("action") action: String = "fetch"
  ): Response<GoogleSheetSyncResponse>

  /**
   * Generic POST method for sync operations (e.g. STORE_TRANSACTION, DELETE_TRANSACTION, SYNC_ALL).
   *
   * @param url The Google Apps Script Web App endpoint URL.
   * @param request The sync request payload with action and data.
   */
  @POST
  suspend fun syncData(
    @Url url: String,
    @Body request: GoogleSheetSyncRequest
  ): Response<GoogleSheetSyncResponse>

  /**
   * Direct POST method without dynamic URL (using configured base URL).
   */
  @POST("exec")
  suspend fun syncDataDefault(
    @Body request: GoogleSheetSyncRequest
  ): Response<GoogleSheetSyncResponse>

  /**
   * Direct GET method without dynamic URL (using configured base URL).
   */
  @GET("exec")
  suspend fun fetchTransactionsDefault(
    @Query("action") action: String = "fetch"
  ): Response<GoogleSheetSyncResponse>
}
