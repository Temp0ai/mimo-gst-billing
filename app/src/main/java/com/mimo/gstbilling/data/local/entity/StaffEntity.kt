package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val role: String = "Staff",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
