package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_items")
data class DeletedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val entityType: String,    // "Invoice", "Party", "Item", "Expense", "Order"
    val entityId: Long,
    val entityName: String,
    val amount: Double,
    val entityData: String,    // Full JSON of the deleted entity for restoration
    val deletedAt: Long = System.currentTimeMillis()
)
