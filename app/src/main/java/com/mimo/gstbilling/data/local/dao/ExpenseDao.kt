package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE companyId = :companyId ORDER BY date DESC")
    fun getExpensesByCompany(companyId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT SUM(amount) FROM expenses WHERE companyId = :companyId")
    suspend fun getTotalExpenses(companyId: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE companyId = :companyId AND category = :category")
    suspend fun getExpensesByCategory(companyId: Long, category: String): Double?
}
