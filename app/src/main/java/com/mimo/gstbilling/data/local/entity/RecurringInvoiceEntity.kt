package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_invoices")
data class RecurringInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val partyName: String,
    val frequency: String = "monthly", // weekly, monthly, quarterly, yearly
    val amount: Double,
    val description: String? = null,
    val invoiceType: String = "sales", // sales or purchase
    val isActive: Boolean = true,
    val nextDueDate: Long,
    val lastGeneratedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
