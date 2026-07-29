package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId"), Index("partyId"), Index("date")]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long = 0,
    val partyName: String = "",
    val date: Long,
    val description: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0,
    val referenceType: String = "", // "invoice", "payment", "expense", "bank_statement", "opening"
    val referenceId: Long = 0,
    val isReconciled: Boolean = false,
    val reconciledWithId: Long = 0, // bank statement entry id
    val source: String = "app", // "app" or "bank_statement"
    val createdAt: Long = System.currentTimeMillis()
)
