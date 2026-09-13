package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
  fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getTransactionById(id: Long): TransactionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete
  suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}
