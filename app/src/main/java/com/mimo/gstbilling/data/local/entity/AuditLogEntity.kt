package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val userId: String,
    val action: String, // "create", "update", "delete"
    val entity: String, // "invoice", "party", "item"
    val entityId: Long,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
