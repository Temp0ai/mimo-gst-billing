package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_variants",
    foreignKeys = [ForeignKey(
        entity = ItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["itemId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ItemVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val variantName: String,
    val sku: String? = null,
    val barcode: String? = null,
    val salePrice: Double,
    val purchasePrice: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val unit: String = "Nos",
    val isActive: Boolean = true
)
