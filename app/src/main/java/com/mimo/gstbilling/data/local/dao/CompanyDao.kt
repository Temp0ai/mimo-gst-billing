package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE isSelected = 1 LIMIT 1")
    fun getSelectedCompany(): Flow<CompanyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Delete
    suspend fun deleteCompany(company: CompanyEntity)

    @Query("UPDATE companies SET isSelected = 0")
    suspend fun clearSelectedCompany()

    @Query("UPDATE companies SET isSelected = 1 WHERE id = :companyId")
    suspend fun setSelectedCompany(companyId: Long)
}
