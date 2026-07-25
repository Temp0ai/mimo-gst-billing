package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.KycEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KycDao {
    @Query("SELECT * FROM kyc_documents WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getKycByCompany(companyId: Long): Flow<List<KycEntity>>

    @Query("SELECT * FROM kyc_documents WHERE companyId = :companyId AND partyId = :partyId ORDER BY createdAt DESC")
    fun getKycByParty(companyId: Long, partyId: Long): Flow<List<KycEntity>>

    @Query("SELECT * FROM kyc_documents WHERE companyId = :companyId AND verificationStatus = :status ORDER BY createdAt DESC")
    fun getKycByStatus(companyId: Long, status: String): Flow<List<KycEntity>>

    @Query("SELECT * FROM kyc_documents WHERE id = :id")
    suspend fun getKycById(id: Long): KycEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKyc(kyc: KycEntity): Long

    @Update
    suspend fun updateKyc(kyc: KycEntity)

    @Delete
    suspend fun deleteKyc(kyc: KycEntity)

    @Query("SELECT * FROM kyc_documents WHERE companyId = :companyId AND partyId = :partyId AND documentType = :documentType LIMIT 1")
    suspend fun getKycByPartyAndType(companyId: Long, partyId: Long, documentType: String): KycEntity?
}
