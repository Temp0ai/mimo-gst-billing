package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val transferDate: Long,
    val chequeNumber: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
