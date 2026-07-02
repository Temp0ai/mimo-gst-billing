package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.PartyGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyGroupDao {
    @Query("SELECT * FROM party_groups WHERE companyId = :companyId ORDER BY name ASC")
    fun getGroupsByCompany(companyId: Long): Flow<List<PartyGroupEntity>>

    @Query("SELECT * FROM party_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): PartyGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: PartyGroupEntity): Long

    @Update
    suspend fun updateGroup(group: PartyGroupEntity)

    @Delete
    suspend fun deleteGroup(group: PartyGroupEntity)

    @Query("SELECT COUNT(*) FROM party_groups WHERE companyId = :companyId")
    suspend fun getGroupCount(companyId: Long): Int
}
