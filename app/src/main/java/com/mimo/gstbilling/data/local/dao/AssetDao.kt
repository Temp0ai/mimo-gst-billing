package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM fixed_assets WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getAssetsByCompany(companyId: Long): Flow<List<AssetEntity>>

    @Query("SELECT * FROM fixed_assets WHERE companyId = :companyId AND category = :category ORDER BY createdAt DESC")
    fun getAssetsByCategory(companyId: Long, category: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM fixed_assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("SELECT SUM(currentValue) FROM fixed_assets WHERE companyId = :companyId")
    suspend fun getTotalAssetValue(companyId: Long): Double?

    @Query("SELECT DISTINCT category FROM fixed_assets WHERE companyId = :companyId ORDER BY category ASC")
    fun getAssetCategories(companyId: Long): Flow<List<String>>
}
