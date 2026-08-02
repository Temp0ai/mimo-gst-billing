package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.OtherIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OtherIncomeDao {
    @Query("SELECT * FROM other_income WHERE companyId = :companyId ORDER BY date DESC")
    fun getOtherIncomeByCompany(companyId: Long): Flow<List<OtherIncomeEntity>>

    @Query("SELECT * FROM other_income WHERE id = :id")
    suspend fun getOtherIncomeById(id: Long): OtherIncomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherIncome(otherIncome: OtherIncomeEntity): Long

    @Update
    suspend fun updateOtherIncome(otherIncome: OtherIncomeEntity)

    @Delete
    suspend fun deleteOtherIncome(otherIncome: OtherIncomeEntity)

    @Query("SELECT SUM(amount) FROM other_income WHERE companyId = :companyId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalOtherIncome(companyId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT COUNT(*) FROM other_income WHERE companyId = :companyId")
    suspend fun getOtherIncomeCount(companyId: Long): Int
}
