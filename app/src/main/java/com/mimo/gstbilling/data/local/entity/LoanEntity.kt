package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long = 1,
    val partyId: Long,
    val loanName: String,
    val loanType: String, // "given" or "received"
    val principalAmount: Double,
    val interestRate: Double,
    val interestType: String, // "fixed" or "reducing"
    val tenure: Int, // months
    val startDate: Long,
    val emiAmount: Double,
    val outstandingAmount: Double,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
