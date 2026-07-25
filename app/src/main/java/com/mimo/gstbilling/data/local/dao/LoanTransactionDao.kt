package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.LoanTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanTransactionDao {
    @Query("SELECT * FROM loan_transactions WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getTransactionsByLoan(loanId: Long): Flow<List<LoanTransactionEntity>>

    @Query("SELECT * FROM loan_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): LoanTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LoanTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: LoanTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: LoanTransactionEntity)

    @Query("SELECT SUM(amount) FROM loan_transactions WHERE loanId = :loanId AND type = 'emi'")
    suspend fun getTotalEmiPaid(loanId: Long): Double?

    @Query("SELECT SUM(amount) FROM loan_transactions WHERE loanId = :loanId AND type = 'prepayment'")
    suspend fun getTotalPrepayment(loanId: Long): Double?
}
