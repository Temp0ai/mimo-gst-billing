package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manufacturing")
data class ManufacturingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val finishedItemName: String,
    val finishedItemQty: Double,
    val rawMaterials: String,
    val totalCost: Double,
    val date: Long,
    val status: String = "completed",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
