package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ManufacturingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManufacturingDao {
    @Query("SELECT * FROM manufacturing WHERE companyId = :companyId ORDER BY date DESC")
    fun getManufacturingByCompany(companyId: Long): Flow<List<ManufacturingEntity>>

    @Query("SELECT * FROM manufacturing WHERE id = :id")
    suspend fun getManufacturingById(id: Long): ManufacturingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManufacturing(entry: ManufacturingEntity): Long

    @Update
    suspend fun updateManufacturing(entry: ManufacturingEntity)

    @Delete
    suspend fun deleteManufacturing(entry: ManufacturingEntity)

    @Query("SELECT SUM(totalCost) FROM manufacturing WHERE companyId = :companyId")
    suspend fun getTotalManufacturingCost(companyId: Long): Double?
}
