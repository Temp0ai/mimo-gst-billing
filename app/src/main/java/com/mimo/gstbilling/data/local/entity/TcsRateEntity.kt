package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tcs_rates")
data class TcsRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val section: String, // "206C"
    val rate: Double,
    val minAmount: Double,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
