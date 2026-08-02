package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estimates",
    indices = [Index(value = ["companyId"]), Index(value = ["partyId"])]
)
data class EstimateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val estimateNumber: String,
    val partyId: Long,
    val partyName: String,
    val amount: Double,
    val date: Long,
    val validUntil: Long,
    val status: String = "pending", // pending, accepted, rejected, expired
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
