package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.AssetDepreciationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDepreciationDao {
    @Query("SELECT * FROM asset_depreciations WHERE assetId = :assetId ORDER BY createdAt DESC")
    fun getDepreciationsByAsset(assetId: Long): Flow<List<AssetDepreciationEntity>>

    @Query("SELECT * FROM asset_depreciations WHERE id = :id")
    suspend fun getDepreciationById(id: Long): AssetDepreciationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepreciation(depreciation: AssetDepreciationEntity): Long

    @Update
    suspend fun updateDepreciation(depreciation: AssetDepreciationEntity)

    @Delete
    suspend fun deleteDepreciation(depreciation: AssetDepreciationEntity)

    @Query("SELECT * FROM asset_depreciations WHERE assetId = :assetId AND period = :period LIMIT 1")
    suspend fun getDepreciationByPeriod(assetId: Long, period: String): AssetDepreciationEntity?
}
