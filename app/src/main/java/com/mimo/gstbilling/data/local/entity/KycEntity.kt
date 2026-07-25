package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kyc_documents")
data class KycEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val partyId: Long,
    val documentType: String, // "PAN", "GSTIN", "Aadhaar", "Bank Account"
    val documentNumber: String,
    val documentPath: String? = null,
    val verificationStatus: String, // "pending", "verified", "rejected"
    val createdAt: Long = System.currentTimeMillis()
)
