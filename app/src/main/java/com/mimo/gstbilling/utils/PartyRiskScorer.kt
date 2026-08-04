package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity

object PartyRiskScorer {

    data class PartyRiskScore(
        val partyId: Long,
        val partyName: String,
        val riskScore: Int,
        val riskLevel: String,
        val totalInvoices: Int,
        val totalAmount: Double,
        val unpaidAmount: Double,
        val avgDaysToPay: Double,
        val paymentReliability: String,
        val factors: List<String>
    )

    fun score(
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>
    ): List<PartyRiskScore> {
        if (parties.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()

        return parties.map { party ->
            val partyInvoices = invoices.filter { it.partyId == party.id }
            if (partyInvoices.isEmpty()) return@map PartyRiskScore(
                party.id, party.name, 30, "MEDIUM", 0, 0.0, party.balance, 0.0, "No Data",
                listOf("No invoice history available")
            )

            val totalAmount = partyInvoices.sumOf { it.totalAmount }
            val unpaidInvoices = partyInvoices.filter { it.paymentStatus != "paid" }
            val unpaidAmount = unpaidInvoices.sumOf { it.totalAmount - it.amountPaid }

            val overdueDays = unpaidInvoices.map { inv ->
                val daysSince = ((now - inv.invoiceDate) / (24 * 60 * 60 * 1000)).toInt()
                daysSince
            }
            val avgDaysToPay = if (overdueDays.isNotEmpty()) overdueDays.average() else 0.0

            val paidInvoices = partyInvoices.filter { it.paymentStatus == "paid" }
            val unpaidRatio = if (totalAmount > 0) unpaidAmount / totalAmount else 0.0

            var riskScore = 0
            val factors = mutableListOf<String>()

            if (unpaidRatio > 0.5) { riskScore += 30; factors.add("High unpaid ratio: ${String.format("%.0f", unpaidRatio * 100)}%") }
            if (avgDaysToPay > 60) { riskScore += 25; factors.add("Avg payment delay: ${String.format("%.0f", avgDaysToPay)} days") }
            if (unpaidAmount > 50000) { riskScore += 15; factors.add("Large unpaid amount: ₹${String.format("%,.0f", unpaidAmount)}") }
            if (partyInvoices.size < 3) { riskScore += 10; factors.add("Limited transaction history") }
            if (overdueDays.any { it > 90 }) { riskScore += 20; factors.add("Has invoices overdue 90+ days") }

            riskScore = riskScore.coerceIn(0, 100)

            val riskLevel = when {
                riskScore >= 70 -> "HIGH"
                riskScore >= 40 -> "MEDIUM"
                else -> "LOW"
            }

            val reliability = when {
                unpaidRatio < 0.1 && avgDaysToPay < 15 -> "Excellent"
                unpaidRatio < 0.2 && avgDaysToPay < 30 -> "Good"
                unpaidRatio < 0.4 -> "Fair"
                else -> "Poor"
            }

            PartyRiskScore(
                partyId = party.id,
                partyName = party.name,
                riskScore = riskScore,
                riskLevel = riskLevel,
                totalInvoices = partyInvoices.size,
                totalAmount = totalAmount,
                unpaidAmount = unpaidAmount,
                avgDaysToPay = String.format("%.0f", avgDaysToPay).toDouble(),
                paymentReliability = reliability,
                factors = factors
            )
        }.sortedByDescending { it.riskScore }.take(20)
    }
}
