package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity

object SmartPricingAdvisor {

    data class PricingSuggestion(
        val itemId: Long,
        val itemName: String,
        val currentPrice: Double,
        val suggestedPrice: Double,
        val reason: String,
        val confidence: Int,
        val potentialRevenue: Double
    )

    fun analyze(
        invoices: List<InvoiceEntity>,
        invoiceItems: List<InvoiceItemEntity>
    ): List<PricingSuggestion> {
        if (invoices.isEmpty()) return emptyList()

        val paidInvoices = invoices.filter { it.paymentStatus == "paid" }
        val unpaidInvoices = invoices.filter { it.paymentStatus == "unpaid" }

        val suggestions = mutableListOf<PricingSuggestion>()

        val itemsByInvoice = invoiceItems.groupBy { it.invoiceId }

        for (invoice in paidInvoices) {
            val items = itemsByInvoice[invoice.id] ?: continue
            for (item in items) {
                val allSameItem = invoiceItems.filter { it.itemName == item.itemName }
                val avgPrice = allSameItem.map { it.price }.average()
                val minPrice = allSameItem.minOfOrNull { it.price } ?: 0.0
                val maxPrice = allSameItem.maxOfOrNull { it.price } ?: 0.0
                val avgQty = allSameItem.map { it.quantity }.average()

                if (avgQty > 1 && item.price < maxPrice * 0.9) {
                    val suggested = (item.price * 1.08).coerceAtMost(maxPrice * 1.05)
                    suggestions.add(
                        PricingSuggestion(
                            itemId = item.itemId,
                            itemName = item.itemName,
                            currentPrice = item.price,
                            suggestedPrice = String.format("%.2f", suggested).toDouble(),
                            reason = "Price is ${String.format("%.1f", ((item.price / avgPrice - 1) * 100))}% below average",
                            confidence = if (allSameItem.size >= 5) 85 else if (allSameItem.size >= 3) 70 else 50,
                            potentialRevenue = (suggested - item.price) * avgQty
                        )
                    )
                }
            }
        }

        val unpaidRatio = if (invoices.isNotEmpty()) unpaidInvoices.size.toDouble() / invoices.size else 0.0
        if (unpaidRatio > 0.4) {
            suggestions.add(
                PricingSuggestion(
                    itemId = -1,
                    itemName = "Overall Pricing",
                    currentPrice = 0.0,
                    suggestedPrice = 0.0,
                    reason = "${String.format("%.0f", unpaidRatio * 100)}% invoices unpaid - consider offering early payment discounts",
                    confidence = 75,
                    potentialRevenue = paidInvoices.sumOf { it.totalAmount } * 0.05
                )
            )
        }

        return suggestions.sortedByDescending { it.confidence }.take(15)
    }
}
