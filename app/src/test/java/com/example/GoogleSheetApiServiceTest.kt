package com.example

import com.example.data.model.TransactionType
import com.example.data.remote.GoogleSheetApiService
import com.example.data.remote.GoogleSheetRetrofitClient
import com.example.data.remote.model.GoogleSheetSyncRequest
import com.example.data.remote.model.GoogleSheetTransactionDto
import com.example.data.remote.model.toGoogleSheetDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GoogleSheetApiServiceTest {

  private lateinit var apiService: GoogleSheetApiService
  private var lastRequestedUrl: String = ""
  private var responseToReturn: String = ""

  @Before
  fun setup() {
    val mockInterceptor = Interceptor { chain ->
      lastRequestedUrl = chain.request().url.toString()
      Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(responseToReturn.toResponseBody("application/json".toMediaType()))
        .build()
    }

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(mockInterceptor)
      .build()

    val moshi = Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()

    apiService = Retrofit.Builder()
      .baseUrl("https://script.google.com/macros/s/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(GoogleSheetApiService::class.java)
  }

  @Test
  fun testPingEndpoint() = runBlocking {
    responseToReturn = """
      {
        "status": "success",
        "message": "Google Sheets API is online and connected!",
        "spreadsheetName": "My Budget 2026"
      }
    """.trimIndent()

    val response = apiService.ping("https://script.google.com/macros/s/test_id/exec")
    assertTrue(response.isSuccessful)
    val body = response.body()
    assertNotNull(body)
    assertEquals("success", body?.status)
    assertEquals("My Budget 2026", body?.spreadsheetName)
    assertTrue(lastRequestedUrl.contains("action=ping"))
  }

  @Test
  fun testFetchTransactionsEndpoint() = runBlocking {
    responseToReturn = """
      {
        "status": "success",
        "count": 2,
        "transactions": [
          {
            "id": 101,
            "title": "Grocery store",
            "amount": 82.5,
            "type": "EXPENSE",
            "category": "Groceries",
            "paymentMethod": "Card",
            "timestamp": 1700000000000,
            "dateFormatted": "Nov 14, 2023",
            "note": "Whole Foods"
          },
          {
            "id": 102,
            "title": "Monthly Salary",
            "amount": 4500.0,
            "type": "INCOME",
            "category": "Salary",
            "paymentMethod": "Direct Deposit",
            "timestamp": 1700000500000,
            "dateFormatted": "Nov 14, 2023",
            "note": "Payroll"
          }
        ]
      }
    """.trimIndent()

    val response = apiService.fetchTransactions("https://script.google.com/macros/s/test_id/exec")
    assertTrue(response.isSuccessful)
    val body = response.body()
    assertNotNull(body)
    assertEquals("success", body?.status)
    assertEquals(2, body?.count)
    assertEquals(2, body?.transactions?.size)

    val firstTx = body?.transactions?.first()?.toTransactionEntity()
    assertNotNull(firstTx)
    assertEquals(101L, firstTx?.id)
    assertEquals(82.5, firstTx?.amount ?: 0.0, 0.001)
    assertEquals(TransactionType.EXPENSE, firstTx?.type)
  }

  @Test
  fun testStoreTransactionEndpoint() = runBlocking {
    responseToReturn = """
      {
        "status": "success",
        "message": "Transaction saved to Google Sheet"
      }
    """.trimIndent()

    val dto = GoogleSheetTransactionDto(
      id = 105L,
      title = "Coffee",
      amount = 4.5,
      type = "EXPENSE",
      category = "Dining",
      paymentMethod = "Apple Pay"
    )
    val request = GoogleSheetSyncRequest(
      action = "STORE_TRANSACTION",
      transaction = dto
    )

    val response = apiService.syncData("https://script.google.com/macros/s/test_id/exec", request)
    assertTrue(response.isSuccessful)
    val body = response.body()
    assertNotNull(body)
    assertEquals("success", body?.status)
    assertEquals("Transaction saved to Google Sheet", body?.message)
  }

  @Test
  fun testDefaultEnvironmentUrlConfigured() {
    assertNotNull(GoogleSheetRetrofitClient.defaultEnvironmentUrl)
  }
}
