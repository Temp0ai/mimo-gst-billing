package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tcs_transactions")
data class TcsTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val invoiceId: Long,
    val partyId: Long,
    val tcsRateId: Long,
    val taxableAmount: Double,
    val tcsAmount: Double,
    val depositionStatus: String, // "pending", "deposited"
    val createdAt: Long = System.currentTimeMillis()
)
