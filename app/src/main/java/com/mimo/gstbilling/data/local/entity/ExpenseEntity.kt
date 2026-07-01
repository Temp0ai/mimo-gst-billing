package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses", indices = [Index(value = ["companyId"])])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val category: String,
    val amount: Double,
    val date: Long,
    val description: String?,
    val paymentMode: String = "cash",
    val createdAt: Long = System.currentTimeMillis()
)
