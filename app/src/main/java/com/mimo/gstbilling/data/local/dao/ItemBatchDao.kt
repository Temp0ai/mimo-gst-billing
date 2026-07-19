package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ItemBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemBatchDao {
    @Query("SELECT * FROM item_batches WHERE companyId = :companyId ORDER BY expiryDate ASC")
    fun getBatchesByCompany(companyId: Long): Flow<List<ItemBatchEntity>>

    @Query("SELECT * FROM item_batches WHERE companyId = :companyId AND itemId = :itemId")
    fun getBatchesByItem(companyId: Long, itemId: Long): Flow<List<ItemBatchEntity>>

    @Query("SELECT * FROM item_batches WHERE companyId = :companyId AND expiryDate IS NOT NULL AND expiryDate <= :date")
    suspend fun getExpiringBatches(companyId: Long, date: Long): List<ItemBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ItemBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: ItemBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: ItemBatchEntity)

    @Query("SELECT SUM(quantity) FROM item_batches WHERE companyId = :companyId AND itemId = :itemId")
    suspend fun getTotalStock(companyId: Long, itemId: Long): Double?
}
