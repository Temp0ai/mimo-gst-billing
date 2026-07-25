package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_depreciations",
    foreignKeys = [ForeignKey(
        entity = AssetEntity::class,
        parentColumns = ["id"],
        childColumns = ["assetId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AssetDepreciationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long,
    val period: String, // "2024-25"
    val depreciationAmount: Double,
    val accumulatedDepreciation: Double,
    val bookValue: Double,
    val createdAt: Long = System.currentTimeMillis()
)
