package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val orderNumber: String,
    val orderType: String,
    val orderDate: Long,
    val deliveryDate: Long?,
    val totalAmount: Double,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val status: String = "pending",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
