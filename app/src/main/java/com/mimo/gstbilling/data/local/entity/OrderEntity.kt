package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val orderNumber: String,
    val orderDate: Long,
    val dueDate: Long? = null,
    val orderType: String = "sales_order",
    val status: String = "pending",
    val subTotal: Double = 0.0,
    val discount: Double = 0.0,
    val discountType: String = "percentage",
    val taxableAmount: Double = 0.0,
    val cgstTotal: Double = 0.0,
    val sgstTotal: Double = 0.0,
    val igstTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
