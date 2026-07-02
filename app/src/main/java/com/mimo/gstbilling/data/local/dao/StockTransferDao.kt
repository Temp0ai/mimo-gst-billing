package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.StockTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransferDao {
    @Query("SELECT * FROM stock_transfers WHERE companyId = :companyId ORDER BY date DESC")
    fun getTransfersByCompany(companyId: Long): Flow<List<StockTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: StockTransferEntity): Long

    @Delete
    suspend fun deleteTransfer(transfer: StockTransferEntity)

    @Query("SELECT COUNT(*) FROM stock_transfers WHERE companyId = :companyId")
    suspend fun getTransferCount(companyId: Long): Int
}
