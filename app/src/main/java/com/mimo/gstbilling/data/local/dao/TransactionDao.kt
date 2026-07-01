package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE companyId = :companyId ORDER BY date DESC")
    fun getTransactionsByCompany(companyId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE companyId = :companyId AND partyId = :partyId ORDER BY date DESC")
    fun getTransactionsByParty(companyId: Long, partyId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE companyId = :companyId AND type = 'credit'")
    suspend fun getTotalCredit(companyId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE companyId = :companyId AND type = 'debit'")
    suspend fun getTotalDebit(companyId: Long): Double?
}
