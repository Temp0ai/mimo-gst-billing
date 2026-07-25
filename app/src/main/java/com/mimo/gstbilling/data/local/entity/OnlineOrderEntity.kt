package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "online_orders")
data class OnlineOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val orderId: String,
    val partyId: Long?,
    val customerName: String,
    val customerPhone: String?,
    val totalAmount: Double,
    val status: String, // "pending", "confirmed", "shipped", "delivered", "cancelled"
    val channel: String, // "website", "whatsapp", "phone"
    val shippingAddress: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
