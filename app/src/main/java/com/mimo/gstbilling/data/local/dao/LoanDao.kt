package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getLoansByCompany(companyId: Long): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE companyId = :companyId AND loanType = :type ORDER BY createdAt DESC")
    fun getLoansByType(companyId: Long, type: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: Long): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    @Query("SELECT * FROM loans WHERE companyId = :companyId AND partyId = :partyId ORDER BY createdAt DESC")
    fun getLoansByParty(companyId: Long, partyId: Long): Flow<List<LoanEntity>>

    @Query("SELECT SUM(outstandingAmount) FROM loans WHERE companyId = :companyId AND loanType = :type")
    suspend fun getTotalOutstanding(companyId: Long, type: String): Double?
}
