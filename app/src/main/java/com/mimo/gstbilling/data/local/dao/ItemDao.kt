package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE companyId = :companyId ORDER BY name ASC")
    fun getItemsByCompany(companyId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE companyId = :companyId AND isService = 0 ORDER BY name ASC")
    fun getProductsByCompany(companyId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE companyId = :companyId AND isService = 1 ORDER BY name ASC")
    fun getServicesByCompany(companyId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE companyId = :companyId AND stockQuantity <= 5 ORDER BY name ASC")
    fun getLowStockItems(companyId: Long): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT COUNT(*) FROM items WHERE companyId = :companyId")
    suspend fun getItemCount(companyId: Long): Int

    @Query("SELECT COUNT(*) FROM items WHERE companyId = :companyId AND isService = 0")
    suspend fun getProductCount(companyId: Long): Int

    @Query("SELECT COUNT(*) FROM items WHERE companyId = :companyId AND isService = 1")
    suspend fun getServiceCount(companyId: Long): Int

    @Query("SELECT COUNT(*) FROM items WHERE companyId = :companyId AND stockQuantity <= 5")
    suspend fun getLowStockCount(companyId: Long): Int
}
