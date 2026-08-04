package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity

object BusinessHealthScorer {

    data class HealthScore(
        val overall: Int,
        val revenue: Int,
        val collections: Int,
        val expenses: Int,
        val growth: Int,
        val rating: String,
        val factors: List<String>,
        val suggestions: List<String>
    )

    fun score(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        parties: List<PartyEntity>
    ): HealthScore {
        val salesInvoices = invoices.filter { it.invoiceType == "sales" }
        if (salesInvoices.isEmpty()) return HealthScore(0, 0, 0, 0, 0, "No Data", listOf("No sales data"), listOf("Start creating invoices"))

        val totalRevenue = salesInvoices.sumOf { it.totalAmount }
        val totalCollected = salesInvoices.sumOf { it.amountPaid }
        val totalExpenses = expenses.sumOf { it.amount }
        val unpaidAmount = totalRevenue - totalCollected

        val revenueScore = when {
            totalRevenue > 1000000 -> 90
            totalRevenue > 500000 -> 75
            totalRevenue > 100000 -> 60
            totalRevenue > 50000 -> 45
            else -> 30
        }

        val collectionRate = if (totalRevenue > 0) totalCollected / totalRevenue else 0.0
        val collectionsScore = when {
            collectionRate > 0.9 -> 95
            collectionRate > 0.75 -> 80
            collectionRate > 0.5 -> 60
            collectionRate > 0.3 -> 40
            else -> 20
        }

        val expenseRatio = if (totalRevenue > 0) totalExpenses / totalRevenue else 1.0
        val expensesScore = when {
            expenseRatio < 0.3 -> 90
            expenseRatio < 0.5 -> 75
            expenseRatio < 0.7 -> 60
            expenseRatio < 0.9 -> 40
            else -> 20
        }

        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val sixtyDaysAgo = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
        val recentSales = salesInvoices.filter { it.invoiceDate > thirtyDaysAgo }.sumOf { it.totalAmount }
        val prevSales = salesInvoices.filter { it.invoiceDate in sixtyDaysAgo..thirtyDaysAgo }.sumOf { it.totalAmount }
        val growthRate = if (prevSales > 0) (recentSales - prevSales) / prevSales else 0.0
        val growthScore = when {
            growthRate > 0.2 -> 90
            growthRate > 0.05 -> 75
            growthRate > -0.05 -> 60
            growthRate > -0.2 -> 40
            else -> 20
        }

        val overall = ((revenueScore * 0.25 + collectionsScore * 0.30 + expensesScore * 0.20 + growthScore * 0.25)).toInt()

        val rating = when {
            overall >= 80 -> "Excellent"
            overall >= 65 -> "Good"
            overall >= 50 -> "Fair"
            overall >= 35 -> "Needs Attention"
            else -> "Critical"
        }

        val factors = mutableListOf<String>()
        if (collectionRate < 0.7) factors.add("Low collection rate: ${String.format("%.0f", collectionRate * 100)}%")
        if (expenseRatio > 0.7) factors.add("High expense ratio: ${String.format("%.0f", expenseRatio * 100)}%")
        if (growthRate < 0) factors.add("Revenue declining: ${String.format("%.1f", growthRate * 100)}%")
        if (unpaidAmount > totalRevenue * 0.3) factors.add("High receivables: ₹${String.format("%,.0f", unpaidAmount)}")
        if (parties.isEmpty()) factors.add("No parties registered")

        val suggestions = mutableListOf<String>()
        if (collectionRate < 0.7) suggestions.add("Follow up on unpaid invoices")
        if (expenseRatio > 0.5) suggestions.add("Review and reduce expenses")
        if (growthRate < 0) suggestions.add("Focus on customer acquisition")
        if (unpaidAmount > 0) suggestions.add("Send payment reminders")

        return HealthScore(overall, revenueScore, collectionsScore, expensesScore, growthScore, rating, factors, suggestions)
    }
}
