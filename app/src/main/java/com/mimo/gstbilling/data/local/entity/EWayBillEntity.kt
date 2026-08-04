package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eway_bills")
data class EWayBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val ewbNumber: String,
    val invoiceNumber: String,
    val invoiceId: Long? = null,
    val partyId: Long? = null,
    val partyName: String,
    val partyGstin: String? = null,
    val placeOfSupply: String,
    val invoiceValue: Double,
    val hsnCode: String,
    val transporterName: String? = null,
    val transporterGstin: String? = null,
    val vehicleNumber: String? = null,
    val distance: Int? = null,
    val supplyType: String = "Outward",  // Outward / Inward
    val subSupplyType: String = "Supply", // Supply / Job Work / Export / SKD/CKD
    val documentType: String = "Tax Invoice",
    val generatedDate: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
    val status: String = "ACTIVE", // ACTIVE / CANCELLED / EXPIRED
    val qrCodeData: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
