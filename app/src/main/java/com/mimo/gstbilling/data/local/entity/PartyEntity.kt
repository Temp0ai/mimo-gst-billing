package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parties",
    indices = [Index(value = ["companyId"])]
)
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val gstin: String?,
    val address: String?,
    val state: String?,
    val stateCode: String?,
    val balance: Double = 0.0,
    val partyType: String, // Customer, Supplier, Both
    val createdAt: Long = System.currentTimeMillis()
)
