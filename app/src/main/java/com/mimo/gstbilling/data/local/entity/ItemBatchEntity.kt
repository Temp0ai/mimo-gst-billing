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
    val quantity: Double,
    val manufacturingDate: Long? = null,
    val expiryDate: Long? = null,
    val purchasePrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
