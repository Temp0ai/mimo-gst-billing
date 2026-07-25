package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE companyId = :companyId ORDER BY timestamp DESC")
    fun getAuditLogsByCompany(companyId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE companyId = :companyId AND action = :action ORDER BY timestamp DESC")
    fun getAuditLogsByAction(companyId: Long, action: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE companyId = :companyId AND entity = :entity ORDER BY timestamp DESC")
    fun getAuditLogsByEntity(companyId: Long, entity: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE id = :id")
    suspend fun getAuditLogById(id: Long): AuditLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(auditLog: AuditLogEntity): Long

    @Update
    suspend fun updateAuditLog(auditLog: AuditLogEntity)

    @Delete
    suspend fun deleteAuditLog(auditLog: AuditLogEntity)
}
