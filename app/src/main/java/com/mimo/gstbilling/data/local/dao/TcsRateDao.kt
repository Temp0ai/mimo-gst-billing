package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.TcsRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TcsRateDao {
    @Query("SELECT * FROM tcs_rates WHERE companyId = :companyId AND isActive = 1 ORDER BY section ASC")
    fun getTcsRatesByCompany(companyId: Long): Flow<List<TcsRateEntity>>

    @Query("SELECT * FROM tcs_rates WHERE id = :id")
    suspend fun getTcsRateById(id: Long): TcsRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTcsRate(tcsRate: TcsRateEntity): Long

    @Update
    suspend fun updateTcsRate(tcsRate: TcsRateEntity)

    @Delete
    suspend fun deleteTcsRate(tcsRate: TcsRateEntity)

    @Query("SELECT * FROM tcs_rates WHERE companyId = :companyId AND section = :section AND isActive = 1 LIMIT 1")
    suspend fun getTcsRateBySection(companyId: Long, section: String): TcsRateEntity?
}
