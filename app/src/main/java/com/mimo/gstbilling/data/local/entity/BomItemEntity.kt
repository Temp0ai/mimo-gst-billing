package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bom_items")
data class BomItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bomId: Long,
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String = "pcs",
    val costPerUnit: Double = 0.0
)
