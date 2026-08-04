package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity

object SalesTrendPredictor {

    data class MonthlySales(val month: String, val amount: Double, val invoiceCount: Int)
    data class SalesForecast(val month: String, val predictedAmount: Double, val confidence: Int, val trend: String)

    fun predict(invoices: List<InvoiceEntity>): List<SalesForecast> {
        val salesInvoices = invoices.filter { it.invoiceType == "sales" }
        if (salesInvoices.isEmpty()) return emptyList()

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        val monthlySales = salesInvoices.groupBy { dateFormat.format(java.util.Date(it.invoiceDate)) }
            .map { (month, invs) -> MonthlySales(month, invs.sumOf { it.totalAmount }, invs.size) }
            .sortedBy { it.month }

        if (monthlySales.size < 2) return emptyList()

        val amounts = monthlySales.map { it.amount }
        val n = amounts.size
        val avg = amounts.average()
        val trend = (amounts.last() - amounts.first()) / n

        val predictions = mutableListOf<SalesForecast>()
        val cal = java.util.Calendar.getInstance()

        for (i in 1..3) {
            cal.add(java.util.Calendar.MONTH, 1)
            val futureMonth = dateFormat.format(cal.time)

            val predicted = (avg + trend * (n + i) / 2).coerceAtLeast(0.0)
            val confidence = when {
                n >= 6 -> 80
                n >= 3 -> 65
                else -> 50
            }
            val trendDir = when {
                trend > avg * 0.1 -> "Upward"
                trend < -avg * 0.1 -> "Downward"
                else -> "Stable"
            }

            predictions.add(SalesForecast(futureMonth, String.format("%.0f", predicted).toDouble(), confidence, trendDir))
        }

        return predictions
    }
}
