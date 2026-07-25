package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.CashAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashAdjustmentDao {
    @Query("SELECT * FROM cash_adjustments WHERE companyId = :companyId ORDER BY adjustmentDate DESC")
    fun getAdjustmentsByCompany(companyId: Long): Flow<List<CashAdjustmentEntity>>

    @Query("SELECT * FROM cash_adjustments WHERE companyId = :companyId AND type = :type ORDER BY adjustmentDate DESC")
    fun getAdjustmentsByType(companyId: Long, type: String): Flow<List<CashAdjustmentEntity>>

    @Query("SELECT * FROM cash_adjustments WHERE id = :id")
    suspend fun getAdjustmentById(id: Long): CashAdjustmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: CashAdjustmentEntity): Long

    @Update
    suspend fun updateAdjustment(adjustment: CashAdjustmentEntity)

    @Delete
    suspend fun deleteAdjustment(adjustment: CashAdjustmentEntity)

    @Query("SELECT SUM(amount) FROM cash_adjustments WHERE companyId = :companyId AND type = 'in'")
    suspend fun getTotalCashIn(companyId: Long): Double?

    @Query("SELECT SUM(amount) FROM cash_adjustments WHERE companyId = :companyId AND type = 'out'")
    suspend fun getTotalCashOut(companyId: Long): Double?
}
