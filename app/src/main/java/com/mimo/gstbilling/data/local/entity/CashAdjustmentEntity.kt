package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_adjustments")
data class CashAdjustmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val type: String, // "in" or "out"
    val amount: Double,
    val adjustmentDate: Long,
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
