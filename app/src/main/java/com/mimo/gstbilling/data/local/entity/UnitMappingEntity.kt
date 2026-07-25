package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit_mappings")
data class UnitMappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val fromUnit: String,
    val toUnit: String,
    val conversionFactor: Double,
    val createdAt: Long = System.currentTimeMillis()
)
