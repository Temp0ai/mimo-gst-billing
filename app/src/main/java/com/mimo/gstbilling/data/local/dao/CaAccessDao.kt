package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.CaAccessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaAccessDao {
    @Query("SELECT * FROM ca_access WHERE companyId = :companyId AND isActive = 1")
    fun getActiveCaList(companyId: Long): Flow<List<CaAccessEntity>>

    @Query("SELECT * FROM ca_access WHERE companyId = :companyId")
    fun getAllCaList(companyId: Long): Flow<List<CaAccessEntity>>

    @Query("SELECT * FROM ca_access WHERE id = :id")
    suspend fun getById(id: Long): CaAccessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ca: CaAccessEntity): Long

    @Update
    suspend fun update(ca: CaAccessEntity)

    @Delete
    suspend fun delete(ca: CaAccessEntity)

    @Query("UPDATE ca_access SET lastSharedAt = :timestamp WHERE id = :id")
    suspend fun updateLastShared(id: Long, timestamp: Long)
}
