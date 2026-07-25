package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ChequeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques WHERE companyId = :companyId ORDER BY chequeDate DESC")
    fun getChequesByCompany(companyId: Long): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE companyId = :companyId AND type = :type ORDER BY chequeDate DESC")
    fun getChequesByType(companyId: Long, type: String): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE companyId = :companyId AND status = :status ORDER BY chequeDate DESC")
    fun getChequesByStatus(companyId: Long, status: String): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE id = :id")
    suspend fun getChequeById(id: Long): ChequeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: ChequeEntity): Long

    @Update
    suspend fun updateCheque(cheque: ChequeEntity)

    @Delete
    suspend fun deleteCheque(cheque: ChequeEntity)

    @Query("SELECT SUM(amount) FROM cheques WHERE companyId = :companyId AND type = :type AND status = 'pending'")
    suspend fun getPendingTotal(companyId: Long, type: String): Double?

    @Query("SELECT * FROM cheques WHERE companyId = :companyId AND partyId = :partyId ORDER BY chequeDate DESC")
    fun getChequesByParty(companyId: Long, partyId: Long): Flow<List<ChequeEntity>>
}
