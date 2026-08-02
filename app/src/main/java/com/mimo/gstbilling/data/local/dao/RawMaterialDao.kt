package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.RawMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawMaterialDao {
    @Query("SELECT * FROM raw_materials WHERE companyId = :companyId ORDER BY name ASC")
    fun getRawMaterialsByCompany(companyId: Long): Flow<List<RawMaterialEntity>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getRawMaterialById(id: Long): RawMaterialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterial(rawMaterial: RawMaterialEntity): Long

    @Update
    suspend fun updateRawMaterial(rawMaterial: RawMaterialEntity)

    @Delete
    suspend fun deleteRawMaterial(rawMaterial: RawMaterialEntity)

    @Query("SELECT COUNT(*) FROM raw_materials WHERE companyId = :companyId")
    suspend fun getRawMaterialCount(companyId: Long): Int
}
