package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discount_configs")
data class DiscountConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val type: String, // "item_level", "party_level", "bill_level"
    val value: Double, // percentage or flat amount
    val valueType: String = "percentage", // "percentage" or "flat"
    val itemId: Long? = null, // null for bill-level
    val partyId: Long? = null, // null for item-level
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
