package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [Transaction] entity providing full CRUD operations.
 */
@Dao
interface TransactionDao {

  // ==========================================
  // CREATE (Insert) Operations
  // ==========================================

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(transaction: Transaction): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: Transaction): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<Transaction>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<Transaction>)

  // ==========================================
  // READ (Query) Operations
  // ==========================================

  @Query("SELECT * FROM transactions ORDER BY date DESC")
  fun getAll(): Flow<List<Transaction>>

  @Query("SELECT * FROM transactions ORDER BY date DESC")
  fun getAllTransactions(): Flow<List<Transaction>>

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getById(id: Long): Transaction?

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getTransactionById(id: Long): Transaction?

  @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
  fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

  @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
  fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>>

  // ==========================================
  // UPDATE Operations
  // ==========================================

  @Update
  suspend fun update(transaction: Transaction)

  @Update
  suspend fun updateTransaction(transaction: Transaction)

  // ==========================================
  // DELETE Operations
  // ==========================================

  @Delete
  suspend fun delete(transaction: Transaction)

  @Delete
  suspend fun deleteTransaction(transaction: Transaction)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun deleteAll()

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}
