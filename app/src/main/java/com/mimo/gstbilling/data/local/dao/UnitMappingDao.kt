package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.UnitMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitMappingDao {
    @Query("SELECT * FROM unit_mappings WHERE companyId = :companyId ORDER BY fromUnit ASC")
    fun getUnitMappingsByCompany(companyId: Long): Flow<List<UnitMappingEntity>>

    @Query("SELECT * FROM unit_mappings WHERE id = :id")
    suspend fun getUnitMappingById(id: Long): UnitMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitMapping(unitMapping: UnitMappingEntity): Long

    @Update
    suspend fun updateUnitMapping(unitMapping: UnitMappingEntity)

    @Delete
    suspend fun deleteUnitMapping(unitMapping: UnitMappingEntity)

    @Query("SELECT * FROM unit_mappings WHERE companyId = :companyId AND fromUnit = :fromUnit AND toUnit = :toUnit LIMIT 1")
    suspend fun getUnitMappingByUnits(companyId: Long, fromUnit: String, toUnit: String): UnitMappingEntity?
}
