package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_of_materials")
data class BillOfMaterialsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val outputItemId: Long,
    val outputItemName: String,
    val outputQuantity: Double = 1.0,
    val estimatedCost: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
