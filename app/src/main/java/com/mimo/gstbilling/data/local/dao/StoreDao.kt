package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores WHERE companyId = :companyId ORDER BY name ASC")
    fun getStoresByCompany(companyId: Long): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :storeId")
    suspend fun getStoreById(storeId: Long): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity): Long

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Delete
    suspend fun deleteStore(store: StoreEntity)

    @Query("SELECT COUNT(*) FROM stores WHERE companyId = :companyId")
    suspend fun getStoreCount(companyId: Long): Int
}
