package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "loan_transactions",
    foreignKeys = [ForeignKey(
        entity = LoanEntity::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoanTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val amount: Double,
    val type: String, // "emi", "prepayment", "penalty"
    val paymentDate: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
