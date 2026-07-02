package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transfers")
data class StockTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val fromStoreId: Long,
    val toStoreId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val date: Long,
    val status: String = "completed",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
