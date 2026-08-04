package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.text.SimpleDateFormat
import java.util.*

object PaymentPatternAnalyzer {

    data class PaymentPattern(
        val partyId: Long,
        val partyName: String,
        val avgDaysToPay: Double,
        val preferredDayOfWeek: String,
        val preferredMonth: String,
        val onTimeRate: Double,
        val avgPaymentAmount: Double,
        val totalPayments: Int,
        val prediction: String
    )

    fun analyze(
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>
    ): List<PaymentPattern> {
        val paidInvoices = invoices.filter { it.paymentStatus == "paid" && it.invoiceType == "sales" }
        if (paidInvoices.isEmpty()) return emptyList()

        val dateFormat = SimpleDateFormat("EEEE", Locale.US)
        val monthFormat = SimpleDateFormat("MMMM", Locale.US)

        return parties.mapNotNull { party ->
            val partyPaid = paidInvoices.filter { it.partyId == party.id }
            if (partyPaid.size < 2) return@mapNotNull null

            val daysToPay = partyPaid.map { inv ->
                ((inv.updatedAt - inv.invoiceDate) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
            }

            val avgDays = daysToPay.average()
            val onTimePayments = daysToPay.count { it <= 30 }
            val onTimeRate = if (partyPaid.isNotEmpty()) onTimePayments.toDouble() / partyPaid.size else 0.0

            val payDays = partyPaid.map { Date(it.updatedAt) }
            val dayOfWeek = payDays.groupBy { dateFormat.format(it) }.maxByOrNull { it.value.size }?.key ?: "Unknown"
            val month = payDays.groupBy { monthFormat.format(it) }.maxByOrNull { it.value.size }?.key ?: "Unknown"

            val avgPayment = partyPaid.sumOf { it.amountPaid } / partyPaid.size

            val prediction = when {
                avgDays <= 7 && onTimeRate > 0.9 -> "Pays very quickly - reliable"
                avgDays <= 15 && onTimeRate > 0.8 -> "Pays on time - good customer"
                avgDays <= 30 && onTimeRate > 0.6 -> "Pays within terms - normal"
                avgDays <= 45 -> "Often delays - follow up needed"
                else -> "Frequently late - consider advance payment"
            }

            PaymentPattern(
                partyId = party.id,
                partyName = party.name,
                avgDaysToPay = String.format("%.0f", avgDays).toDouble(),
                preferredDayOfWeek = dayOfWeek,
                preferredMonth = month,
                onTimeRate = String.format("%.0f", onTimeRate * 100).toDouble(),
                avgPaymentAmount = String.format("%.0f", avgPayment).toDouble(),
                totalPayments = partyPaid.size,
                prediction = prediction
            )
        }.sortedBy { it.avgDaysToPay }.take(20)
    }
}
