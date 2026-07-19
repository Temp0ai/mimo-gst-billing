package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transfers")
data class StockTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val fromWarehouseId: Long,
    val toWarehouseId: Long,
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val transferDate: Long,
    val notes: String? = null,
    val status: String = "completed", // pending, completed, cancelled
    val createdAt: Long = System.currentTimeMillis()
)
