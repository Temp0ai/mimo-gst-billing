package com.mimo.gstbilling.utils

import android.content.Context
import android.net.Uri
import com.mimo.gstbilling.data.local.entity.LedgerEntryEntity
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

data class BankTxn(
    val date: String,
    val description: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val reference: String = ""
)

object BankStatementParser {

    fun parseCsv(context: Context, uri: Uri): List<BankTxn> {
        val txns = mutableListOf<BankTxn>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var isFirstLine = true
                while (reader.readLine().also { line = it } != null) {
                    if (isFirstLine) { isFirstLine = false; continue }
                    val parts = line?.split(",")?.map { it.trim().trim('"') } ?: continue
                    if (parts.size < 4) continue
                    val date = parts[0]
                    val description = parts[1]
                    val debit = parseAmount(parts.getOrElse(2) { "0" })
                    val credit = parseAmount(parts.getOrElse(3) { "0" })
                    val balance = parseAmount(parts.getOrElse(4) { "0" })
                    txns.add(BankTxn(date, description, debit, credit, balance))
                }
            }
        } catch (_: Exception) {}
        return txns
    }

    fun parsePdf(context: Context, uri: Uri): List<BankTxn> {
        val txns = mutableListOf<BankTxn>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val doc = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val text = stripper.getText(doc)
                doc.close()
                val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                for (line in lines) {
                    val txn = parseBankLine(line)
                    if (txn != null) txns.add(txn)
                }
            }
        } catch (_: Exception) {}
        return txns
    }

    private fun parseBankLine(line: String): BankTxn? {
        // Common formats:
        // DD/MM/YYYY  Description  Debit  Credit  Balance
        // DD-MM-YYYY  Description  Amount  Balance
        val datePatterns = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "dd MMM yyyy", "MM/dd/yyyy")
        for (pattern in datePatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val dateEndIdx = findDateEnd(line, pattern)
                if (dateEndIdx > 0) {
                    val dateStr = line.substring(0, dateEndIdx).trim()
                    val rest = line.substring(dateEndIdx).trim()
                    val parts = rest.split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        val descParts = parts.dropLast(3)
                        val description = if (descParts.isNotEmpty()) descParts.joinToString(" ") else rest
                        val debit = parseAmount(parts.getOrElse(parts.size - 3) { "0" })
                        val credit = parseAmount(parts.getOrElse(parts.size - 2) { "0" })
                        val balance = parseAmount(parts.last())
                        return BankTxn(dateStr, description, debit, credit, balance)
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun findDateEnd(line: String, pattern: String): Int {
        val regex = when {
            pattern.contains("dd/MM/yyyy") -> "\\d{2}/\\d{2}/\\d{4}"
            pattern.contains("dd-MM-yyyy") -> "\\d{2}-\\d{2}-\\d{4}"
            pattern.contains("yyyy-MM-dd") -> "\\d{4}-\\d{2}-\\d{2}"
            pattern.contains("dd MMM yyyy") -> "\\d{2}\\s+\\w{3}\\s+\\d{4}"
            pattern.contains("MM/dd/yyyy") -> "\\d{2}/\\d{2}/\\d{4}"
            else -> return -1
        }
        val match = Regex("^$regex").find(line) ?: return -1
        return match.range.last + 1
    }

    fun parseAmount(s: String): Double {
        return s.replace("₹", "").replace(",", "").replace("INR", "").trim().toDoubleOrNull() ?: 0.0
    }

    fun convertToLedgerEntries(
        txns: List<BankTxn>,
        companyId: Long,
        dateParser: (String) -> Long = { parseDateToTimestamp(it) }
    ): List<LedgerEntryEntity> {
        return txns.map { txn ->
            LedgerEntryEntity(
                companyId = companyId,
                partyName = txn.description,
                date = dateParser(txn.date),
                description = txn.description,
                debit = txn.debit,
                credit = txn.credit,
                balance = txn.balance,
                source = "bank_statement",
                referenceType = "bank_import"
            )
        }
    }

    private fun parseDateToTimestamp(dateStr: String): Long {
        val patterns = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "dd MMM yyyy", "MM/dd/yyyy")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                return sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    fun matchTransactions(
        appEntries: List<LedgerEntryEntity>,
        bankEntries: List<LedgerEntryEntity>,
        tolerance: Double = 1.0
    ): List<Pair<LedgerEntryEntity, LedgerEntryEntity?>> {
        return appEntries.map { app ->
            val match = bankEntries.find { bank ->
                !bank.isReconciled &&
                kotlin.math.abs(app.debit - bank.debit) < tolerance &&
                kotlin.math.abs(app.credit - bank.credit) < tolerance &&
                kotlin.math.abs(app.date - bank.date) < 7 * 24 * 60 * 60 * 1000 // 7 days
            }
            Pair(app, match)
        }
    }
}
