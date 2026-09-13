package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.TransactionDao
import com.example.data.database.AppDatabase
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionDaoTest {

  private lateinit var database: AppDatabase
  private lateinit var transactionDao: TransactionDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    transactionDao = database.transactionDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    database.close()
  }

  @Test
  fun testInsertAndGetTransaction() = runBlocking {
    val tx = Transaction(
      id = 0,
      amount = 125.50,
      category = "Groceries",
      date = 1700000000000L,
      description = "Weekly supermarket run"
    )

    val insertedId = transactionDao.insert(tx)
    assertTrue(insertedId > 0)

    val fetched = transactionDao.getById(insertedId)
    assertNotNull(fetched)
    assertEquals(125.50, fetched!!.amount, 0.001)
    assertEquals("Groceries", fetched.category)
    assertEquals(1700000000000L, fetched.date)
    assertEquals("Weekly supermarket run", fetched.description)
  }

  @Test
  fun testGetAllTransactions() = runBlocking {
    val tx1 = Transaction(amount = 45.0, category = "Dining", date = 1000L, description = "Lunch")
    val tx2 = Transaction(amount = 15.0, category = "Transport", date = 2000L, description = "Metro ticket")

    transactionDao.insert(tx1)
    transactionDao.insert(tx2)

    val all = transactionDao.getAll().first()
    assertEquals(2, all.size)
    // Ordered by date DESC
    assertEquals("Transport", all[0].category)
    assertEquals("Dining", all[1].category)
  }

  @Test
  fun testUpdateTransaction() = runBlocking {
    val tx = Transaction(amount = 50.0, category = "Entertainment", date = 1000L, description = "Movies")
    val id = transactionDao.insert(tx)

    val updatedTx = Transaction(
      id = id,
      amount = 75.0,
      category = "Entertainment",
      date = 1000L,
      description = "Movies and Snacks"
    )
    transactionDao.update(updatedTx)

    val result = transactionDao.getById(id)
    assertNotNull(result)
    assertEquals(75.0, result!!.amount, 0.001)
    assertEquals("Movies and Snacks", result.description)
  }

  @Test
  fun testDeleteTransaction() = runBlocking {
    val tx = Transaction(amount = 20.0, category = "Coffee", date = 1000L, description = "Espresso")
    val id = transactionDao.insert(tx)

    assertNotNull(transactionDao.getById(id))

    val savedTx = transactionDao.getById(id)!!
    transactionDao.delete(savedTx)

    assertNull(transactionDao.getById(id))
  }

  @Test
  fun testDeleteById() = runBlocking {
    val tx = Transaction(amount = 30.0, category = "Utilities", date = 1000L, description = "Water bill")
    val id = transactionDao.insert(tx)

    transactionDao.deleteById(id)

    assertNull(transactionDao.getById(id))
  }
}
