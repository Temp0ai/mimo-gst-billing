package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.DeletedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedItemDao {
    @Query("SELECT * FROM deleted_items WHERE companyId = :companyId ORDER BY deletedAt DESC")
    fun getDeletedItems(companyId: Long): Flow<List<DeletedItemEntity>>

    @Query("SELECT * FROM deleted_items WHERE companyId = :companyId AND entityType = :type ORDER BY deletedAt DESC")
    fun getDeletedItemsByType(companyId: Long, type: String): Flow<List<DeletedItemEntity>>

    @Query("SELECT COUNT(*) FROM deleted_items WHERE companyId = :companyId")
    fun getDeletedCount(companyId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deletedItem: DeletedItemEntity)

    @Query("DELETE FROM deleted_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM deleted_items WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: Long)

    @Query("SELECT * FROM deleted_items WHERE id = :id")
    suspend fun getById(id: Long): DeletedItemEntity?
}
