package com.mimo.gstbilling.utils

import android.content.Context
import android.net.Uri
import com.mimo.gstbilling.data.local.AppDatabase
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    data class ImportResult(val success: Int, val failed: Int, val errors: List<String>)

    suspend fun importItems(uri: Uri): ImportResult {
        val errors = mutableListOf<String>()
        var success = 0
        var failed = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                if (lines.isEmpty()) return ImportResult(0, 0, listOf("Empty file"))
                val header = lines[0].lowercase().split(",").map { it.trim() }
                val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("item") }
                val hsnIdx = header.indexOfFirst { it.contains("hsn") }
                val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("rate") || it.contains("sale") }
                val gstIdx = header.indexOfFirst { it.contains("gst") || it.contains("tax") }
                val unitIdx = header.indexOfFirst { it.contains("unit") }
                for (i in 1 until lines.size) {
                    try {
                        val cols = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
                        val name = cols.getOrElse(nameIdx) { "" }
                        if (name.isBlank()) { failed++; errors.add("Row ${i + 1}: empty name"); continue }
                        val item = ItemEntity(
                            companyId = 1L,
                            name = name,
                            hsnCode = cols.getOrElse(hsnIdx) { "" },
                            description = null,
                            salePrice = cols.getOrElse(priceIdx) { "0" }.toDoubleOrNull() ?: 0.0,
                            gstRate = cols.getOrElse(gstIdx) { "0" }.toDoubleOrNull() ?: 0.0,
                            unit = cols.getOrElse(unitIdx) { "PCS" },
                            stockQuantity = 0.0
                        )
                        db.itemDao().insertItem(item)
                        success++
                    } catch (e: Exception) {
                        failed++
                        errors.add("Row ${i + 1}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            errors.add("Error reading file: ${e.message}")
        }
        return ImportResult(success, failed, errors)
    }

    suspend fun importParties(uri: Uri): ImportResult {
        val errors = mutableListOf<String>()
        var success = 0
        var failed = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                if (lines.isEmpty()) return ImportResult(0, 0, listOf("Empty file"))
                val header = lines[0].lowercase().split(",").map { it.trim() }
                val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("party") }
                val phoneIdx = header.indexOfFirst { it.contains("phone") || it.contains("mobile") }
                val gstinIdx = header.indexOfFirst { it.contains("gstin") || it.contains("gst") }
                val emailIdx = header.indexOfFirst { it.contains("email") }
                val addrIdx = header.indexOfFirst { it.contains("address") || it.contains("addr") }
                val typeIdx = header.indexOfFirst { it.contains("type") || it.contains("party type") }
                for (i in 1 until lines.size) {
                    try {
                        val cols = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
                        val name = cols.getOrElse(nameIdx) { "" }
                        if (name.isBlank()) { failed++; errors.add("Row ${i + 1}: empty name"); continue }
                        val party = PartyEntity(
                            companyId = 1L,
                            name = name,
                            phone = cols.getOrElse(phoneIdx) { "" },
                            gstin = cols.getOrElse(gstinIdx) { "" },
                            email = cols.getOrElse(emailIdx) { "" },
                            address = cols.getOrElse(addrIdx) { "" },
                            state = null,
                            stateCode = null,
                            balance = 0.0,
                            partyType = cols.getOrElse(typeIdx) { "Customer" }
                        )
                        db.partyDao().insertParty(party)
                        success++
                    } catch (e: Exception) {
                        failed++
                        errors.add("Row ${i + 1}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            errors.add("Error reading file: ${e.message}")
        }
        return ImportResult(success, failed, errors)
    }
}
