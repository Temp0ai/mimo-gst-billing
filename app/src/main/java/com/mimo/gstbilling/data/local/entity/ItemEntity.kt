package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [Index(value = ["companyId"])]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val name: String,
    val hsnCode: String?,
    val description: String?,
    val salePrice: Double,
    val purchasePrice: Double = 0.0,
    val gstRate: Double, // 0, 5, 12, 18, 28
    val unit: String, // Pcs, Kg, Ltr, etc.
    val stockQuantity: Double = 0.0,
    val isService: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
