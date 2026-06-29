package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions", indices = [Index(value = ["companyId"]), Index(value = ["partyId"])])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val invoiceId: Long? = null,
    val amount: Double,
    val type: String, // credit, debit
    val mode: String, // cash, bank, upi, cheque
    val description: String?,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
