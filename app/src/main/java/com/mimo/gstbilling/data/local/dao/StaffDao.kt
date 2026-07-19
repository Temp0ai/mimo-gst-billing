package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE companyId = :companyId ORDER BY name ASC")
    fun getStaffByCompany(companyId: Long): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: Long): StaffEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)

    @Query("SELECT COUNT(*) FROM staff WHERE companyId = :companyId")
    suspend fun getStaffCount(companyId: Long): Int
}
