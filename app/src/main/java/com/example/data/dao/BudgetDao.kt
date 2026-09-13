package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
  @Query("SELECT * FROM budgets ORDER BY category ASC")
  fun getAllBudgets(): Flow<List<BudgetEntity>>

  @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
  suspend fun getBudgetByCategory(category: String): BudgetEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: BudgetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudgets(budgets: List<BudgetEntity>)

  @Update
  suspend fun updateBudget(budget: BudgetEntity)

  @Delete
  suspend fun deleteBudget(budget: BudgetEntity)

  @Query("DELETE FROM budgets WHERE id = :id")
  suspend fun deleteBudgetById(id: Long)

  @Query("DELETE FROM budgets")
  suspend fun clearAll()
}
