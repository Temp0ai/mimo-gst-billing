package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity

object CustomerChurnPredictor {

    data class ChurnRisk(
        val partyId: Long,
        val partyName: String,
        val riskLevel: String,
        val riskScore: Int,
        val lastPurchaseDays: Int,
        val avgPurchaseFrequency: Double,
        val totalSpent: Double,
        val reason: String
    )

    fun analyze(
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>
    ): List<ChurnRisk> {
        if (parties.isEmpty() || invoices.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val salesInvoices = invoices.filter { it.invoiceType == "sales" }

        return parties.mapNotNull { party ->
            val partyInvoices = salesInvoices.filter { it.partyId == party.id }
            if (partyInvoices.isEmpty()) return@mapNotNull null

            val lastInvoice = partyInvoices.maxByOrNull { it.invoiceDate } ?: return@mapNotNull null
            val lastPurchaseDays = ((now - lastInvoice.invoiceDate) / (24 * 60 * 60 * 1000)).toInt()

            val totalSpent = partyInvoices.sumOf { it.totalAmount }
            val avgInterval = if (partyInvoices.size > 1) {
                val sorted = partyInvoices.sortedBy { it.invoiceDate }
                val intervals = sorted.zipWithNext().map { (a, b) -> (b.invoiceDate - a.invoiceDate) / (24 * 60 * 60 * 1000) }
                intervals.average()
            } else 30.0

            val avgFrequency = if (avgInterval > 0) 30.0 / avgInterval else 1.0

            val riskScore = when {
                lastPurchaseDays > 90 && avgFrequency > 1 -> 90
                lastPurchaseDays > 60 && avgFrequency > 1 -> 75
                lastPurchaseDays > 45 -> 60
                lastPurchaseDays > 30 && avgFrequency < 0.5 -> 50
                lastPurchaseDays > avgInterval * 2 -> 70
                else -> 20
            }

            val riskLevel = when {
                riskScore >= 80 -> "HIGH"
                riskScore >= 50 -> "MEDIUM"
                else -> "LOW"
            }

            val reason = when {
                lastPurchaseDays > 90 -> "No purchase in $lastPurchaseDays days"
                lastPurchaseDays > avgInterval * 2 -> "Overdue by ${lastPurchaseDays - avgInterval.toInt()} days"
                avgFrequency < 0.5 -> "Very low purchase frequency"
                else -> "Purchase pattern declining"
            }

            ChurnRisk(
                partyId = party.id,
                partyName = party.name,
                riskLevel = riskLevel,
                riskScore = riskScore,
                lastPurchaseDays = lastPurchaseDays,
                avgPurchaseFrequency = String.format("%.1f", avgFrequency).toDouble(),
                totalSpent = totalSpent,
                reason = reason
            )
        }.sortedByDescending { it.riskScore }.take(20)
    }
}
