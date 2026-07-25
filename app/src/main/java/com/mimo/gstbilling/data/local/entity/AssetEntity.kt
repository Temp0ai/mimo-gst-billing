package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val assetName: String,
    val category: String,
    val purchaseDate: Long,
    val purchasePrice: Double,
    val salvageValue: Double,
    val usefulLife: Int, // years
    val depreciationMethod: String, // "SLM" or "WDV"
    val currentValue: Double,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
