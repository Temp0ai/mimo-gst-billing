package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["companyId"]), Index(value = ["partyId"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val dueDate: Long?,
    val subTotal: Double,
    val discount: Double = 0.0,
    val discountType: String = "percentage", // percentage or amount
    val taxableAmount: Double,
    val cgstTotal: Double,
    val sgstTotal: Double,
    val igstTotal: Double,
    val cessTotal: Double = 0.0,
    val tcsRate: Double = 0.0,
    val tcsAmount: Double = 0.0,
    val tdsRate: Double = 0.0,
    val tdsAmount: Double = 0.0,
    val roundOff: Double = 0.0,
    val totalAmount: Double,
    val amountPaid: Double = 0.0,
    val notes: String? = null,
    val paymentStatus: String = "unpaid", // unpaid, partial, paid
    val invoiceType: String = "sales", // sales or purchase
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
