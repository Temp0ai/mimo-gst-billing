package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ItemBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemBatchDao {
    @Query("SELECT * FROM item_batches WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getBatchesByCompany(companyId: Long): Flow<List<ItemBatchEntity>>

    @Query("SELECT * FROM item_batches WHERE companyId = :companyId AND itemId = :itemId ORDER BY createdAt DESC")
    fun getBatchesByItem(companyId: Long, itemId: Long): Flow<List<ItemBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ItemBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: ItemBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: ItemBatchEntity)

    @Query("SELECT SUM(quantity) FROM item_batches WHERE companyId = :companyId AND itemId = :itemId")
    suspend fun getTotalStockForItem(companyId: Long, itemId: Long): Double?

    @Query("SELECT * FROM item_batches WHERE companyId = :companyId AND batchNumber = :batchNumber LIMIT 1")
    suspend fun getBatchByNumber(companyId: Long, batchNumber: String): ItemBatchEntity?
}
