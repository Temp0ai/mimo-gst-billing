package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers WHERE companyId = :companyId ORDER BY transferDate DESC")
    fun getTransfersByCompany(companyId: Long): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE id = :id")
    suspend fun getTransferById(id: Long): TransferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Update
    suspend fun updateTransfer(transfer: TransferEntity)

    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)

    @Query("SELECT * FROM transfers WHERE companyId = :companyId AND fromAccount = :account OR toAccount = :account ORDER BY transferDate DESC")
    fun getTransfersByAccount(companyId: Long, account: String): Flow<List<TransferEntity>>

    @Query("SELECT SUM(amount) FROM transfers WHERE companyId = :companyId")
    suspend fun getTotalTransfers(companyId: Long): Double?
}
