package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity

object SmartInvoiceSuggester {

    data class ItemSuggestion(
        val itemName: String,
        val suggestedQty: Double,
        val lastPrice: Double,
        val timesOrdered: Int,
        val lastOrderedDays: Int,
        val partyName: String = ""
    )

    fun suggest(
        partyId: Long,
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>,
        invoiceItems: List<InvoiceItemEntity>
    ): List<ItemSuggestion> {
        val party = parties.find { it.id == partyId } ?: return emptyList()
        val partyInvoices = invoices.filter { it.partyId == partyId && it.invoiceType == "sales" }
        if (partyInvoices.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val itemsByInvoice = invoiceItems.groupBy { it.invoiceId }

        val itemStats = mutableMapOf<String, MutableList<Pair<Double, Long>>>()

        for (invoice in partyInvoices) {
            val items = itemsByInvoice[invoice.id] ?: continue
            for (item in items) {
                itemStats.getOrPut(item.itemName) { mutableListOf() }.add(item.quantity to invoice.invoiceDate)
            }
        }

        return itemStats.map { (name, orders) ->
            val totalQty = orders.sumOf { it.first }
            val avgQty = totalQty / orders.size
            val lastOrder = orders.maxByOrNull { it.second }!!
            val lastDays = ((now - lastOrder.second) / (24 * 60 * 60 * 1000)).toInt()
            val lastPrice = invoiceItems.find { it.itemName == name && it.invoiceId == partyInvoices.lastOrNull()?.id }?.price ?: 0.0

            ItemSuggestion(
                itemName = name,
                suggestedQty = String.format("%.0f", avgQty).toDouble(),
                lastPrice = lastPrice,
                timesOrdered = orders.size,
                lastOrderedDays = lastDays
            )
        }.sortedByDescending { it.timesOrdered }.take(10)
    }
}
