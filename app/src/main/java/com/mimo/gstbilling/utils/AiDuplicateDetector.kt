package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

data class DuplicateGroup(
    val id: String,
    val entries: List<DuplicateEntry>,
    val similarity: Float,
    val type: String
)

data class DuplicateEntry(
    val id: Long,
    val name: String,
    val amount: Double,
    val date: Long,
    val matchScore: Float
)

object AiDuplicateDetector {

    fun detectDuplicateInvoices(invoices: List<InvoiceEntity>): List<DuplicateGroup> {
        if (invoices.size < 2) return emptyList()
        val groups = mutableListOf<DuplicateGroup>()
        val used = mutableSetOf<Int>()
        for (i in invoices.indices) {
            if (i in used) continue
            val group = mutableListOf(i)
            val scores = mutableListOf<Float>()
            for (j in i + 1 until invoices.size) {
                if (j in used) continue
                val a = invoices[i]
                val b = invoices[j]
                if (a.partyId != b.partyId) continue
                val amountSimilarity = if (a.totalAmount == 0.0 && b.totalAmount == 0.0) 1f
                    else (1 - kotlin.math.abs(a.totalAmount - b.totalAmount) / max(a.totalAmount, b.totalAmount)).toFloat().coerceIn(0f, 1f)
                val dateDiff = kotlin.math.abs(a.invoiceDate - b.invoiceDate)
                val dateWindow = TimeUnit.DAYS.toMillis(7)
                val dateSimilarity = if (dateDiff <= dateWindow) 1f - (dateDiff.toFloat() / dateWindow) else 0f
                val numSimilarity = calculateSimilarity(a.invoiceNumber, b.invoiceNumber)
                val score = amountSimilarity * 0.4f + dateSimilarity * 0.3f + numSimilarity * 0.3f
                if (score >= 0.6f) {
                    group.add(j)
                    scores.add(score)
                    used.add(j)
                }
            }
            if (group.size > 1) {
                val avgScore = scores.average().toFloat()
                val entries = group.map { idx ->
                    val inv = invoices[idx]
                    DuplicateEntry(inv.id, inv.invoiceNumber, inv.totalAmount, inv.invoiceDate, avgScore)
                }
                groups.add(DuplicateGroup("invoice_${i}", entries, avgScore, "invoice"))
                used.add(i)
            }
        }
        return groups
    }

    fun detectDuplicateParties(parties: List<PartyEntity>): List<DuplicateGroup> {
        if (parties.size < 2) return emptyList()
        val groups = mutableListOf<DuplicateGroup>()
        val used = mutableSetOf<Int>()
        for (i in parties.indices) {
            if (i in used) continue
            val group = mutableListOf(i)
            val scores = mutableListOf<Float>()
            for (j in i + 1 until parties.size) {
                if (j in used) continue
                val a = parties[i]
                val b = parties[j]
                var score = 0f
                var components = 0
                if (a.gstin.isNotBlank() && b.gstin.isNotBlank()) {
                    val gstSimilarity = calculateSimilarity(a.gstin, b.gstin)
                    if (gstSimilarity > 0.8f) score += gstSimilarity * 2f
                    components += 2
                }
                if (a.phone.isNotBlank() && b.phone.isNotBlank()) {
                    if (a.phone == b.phone) score += 1.5f
                    components += 1
                }
                if (a.email.isNotBlank() && b.email.isNotBlank()) {
                    if (a.email.equals(b.email, ignoreCase = true)) score += 1.5f
                    components += 1
                }
                val nameSimilarity = calculateSimilarity(a.partyName.lowercase(), b.partyName.lowercase())
                score += nameSimilarity
                components += 1
                val finalScore = if (components > 0) score / components else 0f
                if (finalScore >= 0.55f) {
                    group.add(j)
                    scores.add(finalScore)
                    used.add(j)
                }
            }
            if (group.size > 1) {
                val avgScore = scores.average().toFloat()
                val entries = group.map { idx ->
                    val party = parties[idx]
                    DuplicateEntry(party.id, party.partyName, 0.0, 0L, avgScore)
                }
                groups.add(DuplicateGroup("party_${i}", entries, avgScore, "party"))
                used.add(i)
            }
        }
        return groups
    }

    fun detectDuplicateExpenses(expenses: List<ExpenseEntity>): List<DuplicateGroup> {
        if (expenses.size < 2) return emptyList()
        val groups = mutableListOf<DuplicateGroup>()
        val used = mutableSetOf<Int>()
        for (i in expenses.indices) {
            if (i in used) continue
            val group = mutableListOf(i)
            val scores = mutableListOf<Float>()
            for (j in i + 1 until expenses.size) {
                if (j in used) continue
                val a = expenses[i]
                val b = expenses[j]
                val amountSimilarity = if (a.amount == 0.0 && b.amount == 0.0) 1f
                    else (1 - kotlin.math.abs(a.amount - b.amount) / max(a.amount, b.amount)).toFloat().coerceIn(0f, 1f)
                val dateDiff = kotlin.math.abs(a.date - b.date)
                val dateWindow = TimeUnit.DAYS.toMillis(3)
                val dateSimilarity = if (dateDiff <= dateWindow) 1f - (dateDiff.toFloat() / dateWindow) else 0f
                val descSimilarity = calculateSimilarity(a.description.lowercase(), b.description.lowercase())
                val score = amountSimilarity * 0.4f + dateSimilarity * 0.2f + descSimilarity * 0.4f
                if (score >= 0.65f) {
                    group.add(j)
                    scores.add(score)
                    used.add(j)
                }
            }
            if (group.size > 1) {
                val avgScore = scores.average().toFloat()
                val entries = group.map { idx ->
                    val exp = expenses[idx]
                    DuplicateEntry(exp.id, exp.description, exp.amount, exp.date, avgScore)
                }
                groups.add(DuplicateGroup("expense_${i}", entries, avgScore, "expense"))
                used.add(i)
            }
        }
        return groups
    }

    fun calculateSimilarity(str1: String, str2: String): Float {
        if (str1 == str2) return 1f
        val maxLen = max(str1.length, str2.length)
        if (maxLen == 0) return 1f
        val distance = levenshteinDistance(str1, str2)
        return (1f - distance.toFloat() / maxLen).coerceIn(0f, 1f)
    }

    fun findExactDuplicates(amounts: List<Double>, threshold: Double = 0.01): List<List<Int>> {
        if (amounts.size < 2) return emptyList()
        val indexedAmounts = amounts.mapIndexed { index, amount -> Pair(index, amount) }
        val used = mutableSetOf<Int>()
        val groups = mutableListOf<List<Int>>()
        for (i in indexedAmounts.indices) {
            if (i in used) continue
            val group = mutableListOf(indexedAmounts[i].first)
            for (j in i + 1 until indexedAmounts.size) {
                if (j in used) continue
                val diff = kotlin.math.abs(indexedAmounts[i].second - indexedAmounts[j].second)
                if (diff <= threshold) {
                    group.add(indexedAmounts[j].first)
                    used.add(j)
                }
            }
            if (group.size > 1) {
                groups.add(group.sorted())
                used.add(i)
            }
        }
        return groups
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }
}
