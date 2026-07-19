package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val itemId: Long,
    val itemName: String,
    val hsnCode: String? = null,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val gstRate: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)
