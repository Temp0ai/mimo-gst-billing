package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.TcsTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TcsTransactionDao {
    @Query("SELECT * FROM tcs_transactions WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getTcsTransactionsByCompany(companyId: Long): Flow<List<TcsTransactionEntity>>

    @Query("SELECT * FROM tcs_transactions WHERE companyId = :companyId AND depositionStatus = :status ORDER BY createdAt DESC")
    fun getTcsTransactionsByStatus(companyId: Long, status: String): Flow<List<TcsTransactionEntity>>

    @Query("SELECT * FROM tcs_transactions WHERE id = :id")
    suspend fun getTcsTransactionById(id: Long): TcsTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTcsTransaction(tcsTransaction: TcsTransactionEntity): Long

    @Update
    suspend fun updateTcsTransaction(tcsTransaction: TcsTransactionEntity)

    @Delete
    suspend fun deleteTcsTransaction(tcsTransaction: TcsTransactionEntity)

    @Query("SELECT SUM(tcsAmount) FROM tcs_transactions WHERE companyId = :companyId AND depositionStatus = 'pending'")
    suspend fun getPendingTcsTotal(companyId: Long): Double?

    @Query("SELECT * FROM tcs_transactions WHERE companyId = :companyId AND invoiceId = :invoiceId")
    fun getTcsTransactionsByInvoice(companyId: Long, invoiceId: Long): Flow<List<TcsTransactionEntity>>
}
