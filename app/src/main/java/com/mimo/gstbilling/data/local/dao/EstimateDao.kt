package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.EstimateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstimateDao {
    @Query("SELECT * FROM estimates WHERE companyId = :companyId ORDER BY date DESC")
    fun getEstimatesByCompany(companyId: Long): Flow<List<EstimateEntity>>

    @Query("SELECT * FROM estimates WHERE id = :id")
    suspend fun getEstimateById(id: Long): EstimateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: EstimateEntity): Long

    @Update
    suspend fun updateEstimate(estimate: EstimateEntity)

    @Delete
    suspend fun deleteEstimate(estimate: EstimateEntity)

    @Query("UPDATE estimates SET status = :status WHERE id = :id")
    suspend fun updateEstimateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM estimates WHERE companyId = :companyId")
    suspend fun getEstimateCount(companyId: Long): Int
}
