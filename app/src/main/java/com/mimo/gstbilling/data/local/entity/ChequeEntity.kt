package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cheques")
data class ChequeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val partyId: Long,
    val chequeNumber: String,
    val bankName: String,
    val chequeDate: Long,
    val amount: Double,
    val type: String, // "received" or "issued"
    val status: String, // "pending", "cleared", "bounced", "cancelled"
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
