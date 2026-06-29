package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gstin: String?,
    val address: String?,
    val phone: String?,
    val email: String?,
    val businessType: String?, // Retail, Wholesale, Service, etc.
    val state: String?,
    val stateCode: String?,
    val logoUri: String?,
    val signatureUri: String?,
    val bankName: String?,
    val bankAccountNumber: String?,
    val bankIfsc: String?,
    val termsAndConditions: String?,
    val isSelected: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
