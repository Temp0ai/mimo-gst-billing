package com.mimo.gstbilling.utils

import java.net.HttpURLConnection
import java.net.URL

object GstinVerifier {

    private val GSTIN_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")

    private val STATE_CODES = mapOf(
        "01" to "Jammu & Kashmir", "02" to "Himachal Pradesh", "03" to "Punjab",
        "04" to "Chandigarh", "05" to "Uttarakhand", "06" to "Haryana",
        "07" to "Delhi", "08" to "Rajasthan", "09" to "Uttar Pradesh",
        "10" to "Bihar", "11" to "Sikkim", "12" to "Arunachal Pradesh",
        "13" to "Nagaland", "14" to "Manipur", "15" to "Mizoram",
        "16" to "Tripura", "17" to "Meghalaya", "18" to "Assam",
        "19" to "West Bengal", "20" to "Jharkhand", "21" to "Odisha",
        "22" to "Chhattisgarh", "23" to "Madhya Pradesh", "24" to "Gujarat",
        "25" to "Daman & Diu", "26" to "Dadra & Nagar Haveli",
        "27" to "Maharashtra", "28" to "Andhra Pradesh (Old)",
        "29" to "Karnataka", "30" to "Goa", "31" to "Lakshadweep",
        "32" to "Kerala", "33" to "Tamil Nadu", "34" to "Puducherry",
        "35" to "Andaman & Nicobar Islands", "36" to "Telangana",
        "37" to "Andhra Pradesh", "38" to "Ladakh", "97" to "Other Territory"
    )

    private val ENTITY_TYPES = mapOf(
        'A' to "Association of Persons (AOP)",
        'B' to "Body of Individuals (BOI)",
        'C' to "Company",
        'F' to "Firm / LLP",
        'G' to "Government",
        'H' to "HUF",
        'J' to "Artificial Juridical Person",
        'K' to "Karta (HUF)",
        'L' to "Local Authority",
        'P' to "Individual / Proprietorship",
        'R' to "AOP (Trust)",
        'T' to "AOP (Association)"
    )

    private val GSTIN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    data class GstinResult(
        val gstin: String,
        val isValidFormat: Boolean,
        val isValidChecksum: Boolean,
        val stateCode: String?,
        val stateName: String?,
        val entityType: String?,
        val entityChar: Char?,
        val panNumber: String,
        val isLive: Boolean? = null,
        val businessName: String? = null,
        val registrationDate: String? = null,
        val status: String? = null,
        val errorMessage: String? = null
    ) {
        val isFullyValid: Boolean get() = isValidFormat && isValidChecksum
        val displayStatus: String
            get() = when {
                isFullyValid && isLive == true -> "Active"
                isFullyValid && isLive == false -> "Inactive"
                isFullyValid -> "Format Valid"
                isValidFormat -> "Invalid Checksum"
                else -> "Invalid GSTIN"
            }
    }

    fun validateFormat(gstin: String): GstinResult {
        val clean = gstin.trim().uppercase()
        if (clean.length != 15) {
            return GstinResult(
                gstin = clean, isValidFormat = false, isValidChecksum = false,
                stateCode = null, stateName = null, entityType = null, entityChar = null,
                panNumber = "", errorMessage = "GSTIN must be 15 characters (got ${clean.length})"
            )
        }
        if (!GSTIN_REGEX.matches(clean)) {
            return GstinResult(
                gstin = clean, isValidFormat = false, isValidChecksum = false,
                stateCode = null, stateName = null, entityType = null, entityChar = null,
                panNumber = "", errorMessage = "Invalid GSTIN format"
            )
        }
        val stateCode = clean.substring(0, 2)
        val stateName = STATE_CODES[stateCode] ?: "Unknown State"
        val entityChar = clean[13]
        val entityType = ENTITY_TYPES[entityChar] ?: "Unknown"
        val pan = clean.substring(2, 12)
        val checksumValid = verifyCheckDigit(clean)
        return GstinResult(
            gstin = clean, isValidFormat = true, isValidChecksum = checksumValid,
            stateCode = stateCode, stateName = stateName, entityType = entityType,
            entityChar = entityChar, panNumber = pan
        )
    }

    private fun verifyCheckDigit(gstin: String): Boolean {
        val factor = intArrayOf(1, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)
        var sum = 0
        for (i in 0..12) {
            val charIndex = GSTIN_CHARS.indexOf(gstin[i])
            if (charIndex < 0) return false
            sum += charIndex * factor[i]
        }
        val remainder = sum % 36
        val checkChar = GSTIN_CHARS[(36 - remainder) % 36]
        return checkChar == gstin[14]
    }

    fun verifyViaApi(gstin: String): GstinResult {
        val localResult = validateFormat(gstin)
        if (!localResult.isValidFormat) return localResult
        return try {
            val url = URL("https://api gst.in/api/v1/public/searchByGstin?gstin=${gstin}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("User-Agent", "MimoGstBilling/1.0")
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                val tradeName = extractJsonField(body, "tradeNam") ?: extractJsonField(body, "lgnm")
                val regDate = extractJsonField(body, "rgdt")
                val status = extractJsonField(body, "sts")
                val isActive = status?.uppercase() == "ACTIVE"
                localResult.copy(
                    isLive = isActive,
                    businessName = tradeName,
                    registrationDate = regDate,
                    status = status
                )
            } else {
                localResult.copy(isLive = null, errorMessage = "API returned $responseCode")
            }
        } catch (e: Exception) {
            localResult.copy(isLive = null, errorMessage = "Network error: ${e.message}")
        }
    }

    private fun extractJsonField(json: String, key: String): String? {
        val patterns = listOf(
            "\"$key\"\\s*:\\s*\"([^\"]+)\"",
            "\"$key\"\\s*:\\s*([0-9]+)"
        )
        for (pattern in patterns) {
            val match = Regex(pattern).find(json)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    fun getStateName(stateCode: String): String? = STATE_CODES[stateCode]
    fun getEntityType(entityChar: Char): String? = ENTITY_TYPES[entityChar]
}
