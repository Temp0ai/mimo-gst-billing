package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.EWayBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EWayBillDao {
    @Query("SELECT * FROM eway_bills WHERE companyId = :companyId ORDER BY generatedDate DESC")
    fun getEWayBills(companyId: Long): Flow<List<EWayBillEntity>>

    @Query("SELECT * FROM eway_bills WHERE companyId = :companyId AND status = 'ACTIVE' ORDER BY generatedDate DESC")
    fun getActiveEWayBills(companyId: Long): Flow<List<EWayBillEntity>>

    @Query("SELECT * FROM eway_bills WHERE id = :id")
    suspend fun getById(id: Long): EWayBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ewayBill: EWayBillEntity): Long

    @Update
    suspend fun update(ewayBill: EWayBillEntity)

    @Query("UPDATE eway_bills SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelEWayBill(id: Long)

    @Query("SELECT COUNT(*) FROM eway_bills WHERE companyId = :companyId AND status = 'ACTIVE'")
    fun getActiveCount(companyId: Long): Flow<Int>
}
