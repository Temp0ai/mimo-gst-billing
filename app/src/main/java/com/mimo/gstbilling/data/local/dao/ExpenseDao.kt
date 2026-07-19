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

    @Query("SELECT * FROM expenses WHERE companyId = :companyId AND category = :category ORDER BY date DESC")
    fun getExpensesByCategoryFlow(companyId: Long, category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE companyId = :companyId AND date BETWEEN :start AND :end")
    suspend fun getMonthlyTotal(companyId: Long, start: Long, end: Long): Double?

    @Query("SELECT DISTINCT category FROM expenses WHERE companyId = :companyId ORDER BY category ASC")
    fun getExpenseCategories(companyId: Long): Flow<List<String>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?
}
