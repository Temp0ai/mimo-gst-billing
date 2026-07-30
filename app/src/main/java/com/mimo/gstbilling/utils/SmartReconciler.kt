package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.LedgerEntryEntity
import java.text.SimpleDateFormat
import java.util.Locale

data class ReconciliationMatch(
    val appEntry: LedgerEntryEntity,
    val bankEntry: LedgerEntryEntity,
    val score: Float,
    val reasons: List<String>,
    val amountDiff: Double,
    val dayDiff: Int
)

data class ReconciliationSummary(
    val totalMatches: Int,
    val autoReconciled: Int,
    val manualReview: Int,
    val noMatch: Int
)

object SmartReconciler {

    fun findMatches(
        appEntries: List<LedgerEntryEntity>,
        bankEntries: List<LedgerEntryEntity>
    ): List<ReconciliationMatch> {
        val matches = mutableListOf<ReconciliationMatch>()

        for (bank in bankEntries) {
            if (bank.isReconciled) continue
            var bestMatch: ReconciliationMatch? = null
            var bestScore = 0f

            for (app in appEntries) {
                if (app.isReconciled) continue
                val score = calculateScore(app, bank)
                if (score > bestScore && score >= 0.3f) {
                    bestScore = score
                    val reasons = getMatchReasons(app, bank)
                    val amountDiff = kotlin.math.abs(getAmount(app) - getAmount(bank))
                    val dayDiff = daysBetween(app.date, bank.date)
                    bestMatch = ReconciliationMatch(
                        appEntry = app,
                        bankEntry = bank,
                        score = score,
                        reasons = reasons,
                        amountDiff = amountDiff,
                        dayDiff = dayDiff
                    )
                }
            }
            bestMatch?.let { matches.add(it) }
        }

        return matches.sortedByDescending { it.score }
    }

    private fun getAmount(entry: LedgerEntryEntity): Double {
        return if (entry.debit > 0) entry.debit else entry.credit
    }

    private fun calculateScore(app: LedgerEntryEntity, bank: LedgerEntryEntity): Float {
        var score = 0f

        val appAmt = getAmount(app)
        val bankAmt = getAmount(bank)
        val amountDiff = kotlin.math.abs(appAmt - bankAmt)
        val amountPct = if (appAmt > 0) amountDiff / appAmt else 1.0
        when {
            amountDiff < 0.01 -> score += 0.40f
            amountPct < 0.01 -> score += 0.35f
            amountPct < 0.05 -> score += 0.25f
            amountPct < 0.10 -> score += 0.15f
            amountPct < 0.20 -> score += 0.05f
        }

        val dayDiff = daysBetween(app.date, bank.date)
        when (dayDiff) {
            0 -> score += 0.25f
            1 -> score += 0.20f
            2 -> score += 0.15f
            3 -> score += 0.10f
            in 4..7 -> score += 0.05f
        }

        val nameScore = nameSimilarity(app.partyName, bank.description)
        score += (nameScore * 0.20f).toFloat()

        if (app.referenceType.isNotBlank() && bank.description.contains(app.referenceType, ignoreCase = true)) {
            score += 0.15f
        } else if (app.description.isNotBlank() && bank.description.contains(app.description, ignoreCase = true)) {
            score += 0.12f
        }

        return score.coerceIn(0f, 1f)
    }

    private fun nameSimilarity(name1: String, name2: String): Double {
        if (name1.isBlank() || name2.isBlank()) return 0.0
        val n1 = name1.lowercase().trim()
        val n2 = name2.lowercase().trim()

        if (n1 == n2) return 1.0
        if (n2.contains(n1) || n1.contains(n2)) return 0.85

        val words1 = n1.split("\\s+".toRegex()).toSet()
        val words2 = n2.split("\\s+".toRegex()).toSet()
        val intersection = words1.intersect(words2)
        val union = words1.union(words2)
        if (union.isNotEmpty()) {
            return intersection.size.toDouble() / union.size.toDouble()
        }

        val maxLen = maxOf(n1.length, n2.length)
        if (maxLen == 0) return 1.0
        val distance = levenshteinDistance(n1, n2)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun daysBetween(date1: Long, date2: Long): Int {
        val diff = kotlin.math.abs(date1 - date2)
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun getMatchReasons(app: LedgerEntryEntity, bank: LedgerEntryEntity): List<String> {
        val reasons = mutableListOf<String>()
        val amountDiff = kotlin.math.abs(getAmount(app) - getAmount(bank))
        if (amountDiff < 0.01) reasons.add("Exact amount match")
        else if (getAmount(app) > 0 && amountDiff / getAmount(app) < 0.05) reasons.add("Amount within 5%")

        val dayDiff = daysBetween(app.date, bank.date)
        when (dayDiff) {
            0 -> reasons.add("Same day")
            1 -> reasons.add("1 day apart")
            in 2..3 -> reasons.add("$dayDiff days apart")
        }

        if (nameSimilarity(app.partyName, bank.description) > 0.5) {
            reasons.add("Party name matches")
        }

        if (app.description.isNotBlank() && bank.description.contains(app.description, ignoreCase = true)) {
            reasons.add("Reference found")
        }

        return reasons
    }

    fun getSummary(matches: List<ReconciliationMatch>): ReconciliationSummary {
        return ReconciliationSummary(
            totalMatches = matches.size,
            autoReconciled = matches.count { it.score >= 0.8f },
            manualReview = matches.count { it.score in 0.5f..0.79f },
            noMatch = 0
        )
    }

    fun formatTimestamp(ts: Long): String {
        return try {
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(ts)
        } catch (_: Exception) {
            ""
        }
    }
}
