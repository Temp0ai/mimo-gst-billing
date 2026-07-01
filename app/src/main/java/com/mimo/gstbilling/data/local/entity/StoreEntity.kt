package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val address: String?,
    val phone: String?,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
