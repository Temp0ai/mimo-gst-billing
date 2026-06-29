package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "invoice_items",
    primaryKeys = ["invoiceId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = InvoiceEntity::class, parentColumns = ["id"], childColumns = ["invoiceId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    val invoiceId: Long,
    val itemId: Long,
    val itemName: String,
    val hsnCode: String?,
    val quantity: Double,
    val unit: String,
    val price: Double,
    val discount: Double = 0.0,
    val gstRate: Double,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double
)
