package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE companyId = :companyId ORDER BY name ASC")
    fun getPartiesByCompany(companyId: Long): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE companyId = :companyId AND partyType = :type ORDER BY name ASC")
    fun getPartiesByType(companyId: Long, type: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :partyId")
    suspend fun getPartyById(partyId: Long): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(partyEntity: PartyEntity): Long

    @Update
    suspend fun updateParty(partyEntity: PartyEntity)

    @Delete
    suspend fun deleteParty(partyEntity: PartyEntity)

    @Query("SELECT COUNT(*) FROM parties WHERE companyId = :companyId")
    suspend fun getPartyCount(companyId: Long): Int
}
