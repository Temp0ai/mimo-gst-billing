package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_batches")
data class ItemBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val itemId: Long,
    val itemName: String,
    val batchNumber: String,
    val serialNumber: String?,
    val expiryDate: Long?,
    val quantity: Double,
    val purchasePrice: Double,
    val salePrice: Double,
    val manufacturingDate: Long?,
    val createdAt: Long = System.currentTimeMillis()
)
