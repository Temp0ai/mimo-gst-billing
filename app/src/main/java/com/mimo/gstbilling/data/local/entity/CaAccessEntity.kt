package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ca_access")
data class CaAccessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val caName: String,
    val caEmail: String,
    val caPhone: String? = null,
    val caGstin: String? = null,
    val firmName: String? = null,
    val accessLevel: String = "view_only", // view_only, download, full_access
    val lastSharedAt: Long = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
