package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "raw_materials",
    indices = [Index(value = ["companyId"])]
)
data class RawMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val name: String,
    val unit: String,
    val stockQty: Double,
    val costPerUnit: Double,
    val hsnCode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
