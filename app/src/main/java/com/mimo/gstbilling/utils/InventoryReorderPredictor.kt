package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.ItemEntity

object InventoryReorderPredictor {

    data class ReorderAlert(
        val itemId: Long,
        val itemName: String,
        val currentStock: Double,
        val avgDailySales: Double,
        val daysUntilStockout: Int,
        val suggestedReorderQty: Double,
        val urgency: String,
        val reorderValue: Double
    )

    fun predict(
        invoices: List<InvoiceEntity>,
        invoiceItems: List<InvoiceItemEntity>,
        items: List<ItemEntity>
    ): List<ReorderAlert> {
        if (invoices.isEmpty() || items.isEmpty()) return emptyList()

        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val recentInvoices = invoices.filter { it.invoiceDate > thirtyDaysAgo && it.invoiceType == "sales" }

        val itemSales = mutableMapOf<String, Double>()
        val itemsByInvoice = invoiceItems.groupBy { it.invoiceId }

        for (invoice in recentInvoices) {
            val items = itemsByInvoice[invoice.id] ?: continue
            for (item in items) {
                itemSales[item.itemName] = (itemSales[item.itemName] ?: 0.0) + item.quantity
            }
        }

        return items.mapNotNull { item ->
            val totalSold = itemSales[item.name] ?: 0.0
            if (totalSold <= 0) return@mapNotNull null

            val avgDailySales = totalSold / 30.0
            val daysUntilStockout = if (avgDailySales > 0) (item.stockQuantity / avgDailySales).toInt() else 999
            val suggestedReorderQty = avgDailySales * 30 * 1.2

            if (daysUntilStockout <= 30) {
                val urgency = when {
                    daysUntilStockout <= 3 -> "CRITICAL"
                    daysUntilStockout <= 7 -> "HIGH"
                    daysUntilStockout <= 14 -> "MEDIUM"
                    else -> "LOW"
                }
                ReorderAlert(
                    itemId = item.id,
                    itemName = item.name,
                    currentStock = item.stockQuantity,
                    avgDailySales = String.format("%.1f", avgDailySales).toDouble(),
                    daysUntilStockout = daysUntilStockout,
                    suggestedReorderQty = String.format("%.0f", suggestedReorderQty).toDouble(),
                    urgency = urgency,
                    reorderValue = suggestedReorderQty * item.purchasePrice
                )
            } else null
        }.sortedBy { it.daysUntilStockout }.take(20)
    }
}
