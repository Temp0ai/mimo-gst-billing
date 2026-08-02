package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "other_income",
    indices = [Index(value = ["companyId"])]
)
data class OtherIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val source: String, // e.g., "Interest", "Commission", "Rent", "Sale of Asset"
    val amount: Double,
    val date: Long,
    val description: String? = null,
    val referenceNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
