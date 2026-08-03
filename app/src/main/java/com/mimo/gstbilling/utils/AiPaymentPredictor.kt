package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AiPaymentPredictor {

    data class PaymentPrediction(
        val invoiceId: Long,
        val partyName: String,
        val predictedDays: Int,
        val predictedDate: Long,
        val confidence: Float,
        val riskLevel: String,
        val factors: List<String>
    )

    data class PartyPaymentProfile(
        val partyId: Long,
        val partyName: String,
        val averagePaymentDays: Double,
        val paymentConsistency: Float,
        val totalInvoices: Int,
        val paidOnTimeRate: Float,
        val outstandingAmount: Double
    )

    fun predictPaymentDate(
        invoice: InvoiceEntity,
        partyHistory: List<InvoiceEntity>,
        party: PartyEntity?
    ): PaymentPrediction {
        val partyInvoices = partyHistory.filter { it.partyId == invoice.partyId }
        val paidInvoices = partyInvoices.filter { it.paymentStatus == "paid" }

        val factors = mutableListOf<String>()
        var baseDays = 30

        if (paidInvoices.isNotEmpty()) {
            val avgDays = paidInvoices.map { inv ->
                val createdMs = inv.createdAt
                val now = System.currentTimeMillis()
                TimeUnit.MILLISECONDS.toDays(now - createdMs).toDouble()
            }.average()
            baseDays = avgDays.toInt().coerceIn(1, 90)
            factors.add("Historical average: ${avgDays.toInt()} days")
        }

        val onTimeRate = if (paidInvoices.isNotEmpty()) {
            val onTimeCount = paidInvoices.count { inv ->
                val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - inv.createdAt)
                days <= 30
            }
            onTimeCount.toFloat() / paidInvoices.size
        } else {
            0.5f
        }

        if (onTimeRate > 0.8f) {
            baseDays -= 5
            factors.add("Reliable payer (${(onTimeRate * 100).toInt()}% on time)")
        } else if (onTimeRate < 0.4f) {
            baseDays += 10
            factors.add("Frequent late payer (${(onTimeRate * 100).toInt()}% on time)")
        }

        if (invoice.totalAmount > 100000) {
            baseDays += 5
            factors.add("Large invoice amount (₹${invoice.totalAmount.toInt()})")
        } else if (invoice.totalAmount < 5000) {
            baseDays -= 3
            factors.add("Small invoice amount")
        }

        val totalOutstanding = partyInvoices
            .filter { it.paymentStatus != "paid" }
            .sumOf { it.totalAmount }
        if (totalOutstanding > 500000) {
            baseDays += 7
            factors.add("High outstanding balance (₹${totalOutstanding.toInt()})")
        }

        val month = Calendar.getInstance().get(Calendar.MONTH)
        if (month in 2..3) {
            baseDays += 5
            factors.add("Year-end period (typically slower payments)")
        }

        baseDays = baseDays.coerceIn(1, 90)

        val confidence = when {
            paidInvoices.size >= 10 -> 0.85f
            paidInvoices.size >= 5 -> 0.7f
            paidInvoices.size >= 2 -> 0.55f
            else -> 0.4f
        }

        val riskLevel = when {
            baseDays > 45 || onTimeRate < 0.3f -> "High"
            baseDays > 30 || onTimeRate < 0.6f -> "Medium"
            else -> "Low"
        }

        val predictedDate = invoice.createdAt + TimeUnit.DAYS.toMillis(baseDays.toLong())

        return PaymentPrediction(
            invoiceId = invoice.id,
            partyName = party?.name ?: "Unknown",
            predictedDays = baseDays,
            predictedDate = predictedDate,
            confidence = confidence,
            riskLevel = riskLevel,
            factors = factors
        )
    }

    fun generatePartyProfile(
        party: PartyEntity,
        invoices: List<InvoiceEntity>
    ): PartyPaymentProfile {
        val partyInvoices = invoices.filter { it.partyId == party.id }
        val paidInvoices = partyInvoices.filter { it.paymentStatus == "paid" }

        val avgPaymentDays = if (paidInvoices.isNotEmpty()) {
            paidInvoices.map { inv ->
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - inv.createdAt).toDouble()
            }.average()
        } else {
            30.0
        }

        val consistency = if (paidInvoices.size > 1) {
            val days = paidInvoices.map {
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it.createdAt).toDouble()
            }
            val avg = days.average()
            val variance = days.map { (it - avg) * (it - avg) }.average()
            val stdDev = Math.sqrt(variance)
            (1.0 - (stdDev / avg)).coerceIn(0.0, 1.0).toFloat()
        } else {
            0.5f
        }

        val onTimeRate = if (paidInvoices.isNotEmpty()) {
            paidInvoices.count {
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it.createdAt) <= 30
            }.toFloat() / paidInvoices.size
        } else {
            0.5f
        }

        val outstanding = partyInvoices
            .filter { it.paymentStatus != "paid" }
            .sumOf { it.totalAmount }

        return PartyPaymentProfile(
            partyId = party.id,
            partyName = party.name,
            averagePaymentDays = avgPaymentDays,
            paymentConsistency = consistency,
            totalInvoices = partyInvoices.size,
            paidOnTimeRate = onTimeRate,
            outstandingAmount = outstanding
        )
    }

    fun generateRiskScores(
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>
    ): List<Pair<PartyEntity, Float>> {
        return parties.map { party ->
            val profile = generatePartyProfile(party, invoices)
            val riskScore = when {
                profile.paidOnTimeRate < 0.3f -> 0.9f
                profile.paidOnTimeRate < 0.5f -> 0.7f
                profile.paidOnTimeRate < 0.7f -> 0.5f
                profile.averagePaymentDays > 45 -> 0.6f
                profile.averagePaymentDays > 30 -> 0.4f
                else -> 0.2f
            }
            party to riskScore
        }.sortedByDescending { it.second }
    }
}
