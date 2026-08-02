package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AiBusinessInsights {

    data class BusinessInsight(
        val id: String,
        val title: String,
        val description: String,
        val metric: String,
        val trend: String,
        val impact: String,
        val suggestion: String
    )

    data class TopCustomer(
        val partyId: Long,
        val partyName: String,
        val totalAmount: Double,
        val invoiceCount: Int,
        val averageOrderValue: Double
    )

    data class SalesTrend(
        val period: String,
        val amount: Double,
        val changePercent: Double
    )

    data class BusinessMetrics(
        val totalRevenue: Double,
        val totalExpenses: Double,
        val profitMargin: Double,
        val averageOrderValue: Double,
        val customerRetentionRate: Double,
        val topCustomerConcentration: Double
    )

    fun generateInsights(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        parties: List<PartyEntity>
    ): List<BusinessInsight> {
        val insights = mutableListOf<BusinessInsight>()
        val metrics = calculateMetrics(invoices, expenses, parties)
        val trends = getSalesTrends(invoices, 6)
        val topCustomers = getTopCustomers(invoices, parties, 5)

        if (trends.size >= 2) {
            val recentChange = trends.last().changePercent
            val trendDirection = when {
                recentChange > 5.0 -> "up"
                recentChange < -5.0 -> "down"
                else -> "stable"
            }
            insights.add(
                BusinessInsight(
                    id = "revenue_trend_${System.currentTimeMillis()}",
                    title = "Revenue Trend",
                    description = "Revenue has ${if (recentChange > 0) "increased" else "decreased"} by ${String.format("%.1f", kotlin.math.abs(recentChange))}% over the last period.",
                    metric = "₹${String.format("%.2f", metrics.totalRevenue)}",
                    trend = trendDirection,
                    impact = if (recentChange > 0) "positive" else "negative",
                    suggestion = if (recentChange > 0) "Maintain current momentum and consider scaling successful strategies." else "Review pricing strategy and identify areas to reduce costs."
                )
            )
        }

        if (topCustomers.isNotEmpty()) {
            val topConcentration = metrics.topCustomerConcentration
            insights.add(
                BusinessInsight(
                    id = "customer_concentration_${System.currentTimeMillis()}",
                    title = "Customer Concentration",
                    description = "Top customer contributes ${String.format("%.1f", topConcentration)}% of total revenue.",
                    metric = "${topCustomers.first().partyName}",
                    trend = if (topConcentration > 30) "up" else "stable",
                    impact = if (topConcentration > 30) "negative" else "neutral",
                    suggestion = if (topConcentration > 30) "Diversify customer base to reduce dependency on single customer." else "Maintain healthy customer distribution."
                )
            )
        }

        if (metrics.profitMargin < 20) {
            insights.add(
                BusinessInsight(
                    id = "low_margin_${System.currentTimeMillis()}",
                    title = "Low Profit Margin",
                    description = "Current profit margin is ${String.format("%.1f", metrics.profitMargin)}%, which is below healthy threshold.",
                    metric = "${String.format("%.1f", metrics.profitMargin)}%",
                    trend = "down",
                    impact = "negative",
                    suggestion = "Consider increasing prices or reducing operational costs to improve margins."
                )
            )
        }

        if (expenses.isNotEmpty()) {
            val avgExpense = expenses.map { it.amount }.average()
            val highExpenses = expenses.filter { it.amount > avgExpense * 1.5 }
            if (highExpenses.isNotEmpty()) {
                insights.add(
                    BusinessInsight(
                        id = "expense_anomaly_${System.currentTimeMillis()}",
                        title = "Expense Anomaly Detected",
                        description = "Found ${highExpenses.size} expenses significantly above average.",
                        metric = "₹${String.format("%.2f", avgExpense)} avg",
                        trend = "up",
                        impact = "negative",
                        suggestion = "Review high expenses to identify potential cost optimization opportunities."
                    )
                )
            }
        }

        if (metrics.averageOrderValue > 0 && metrics.customerRetentionRate < 50) {
            insights.add(
                BusinessInsight(
                    id = "retention_opportunity_${System.currentTimeMillis()}",
                    title = "Customer Retention Opportunity",
                    description = "Customer retention rate is ${String.format("%.1f", metrics.customerRetentionRate)}%. There's room for improvement.",
                    metric = "${String.format("%.1f", metrics.customerRetentionRate)}%",
                    trend = "down",
                    impact = "negative",
                    suggestion = "Implement customer loyalty programs and follow-up strategies to improve retention."
                )
            )
        }

        if (metrics.totalRevenue > 0) {
            val growthRate = if (trends.size >= 2) {
                ((trends.last().amount - trends.first().amount) / trends.first().amount) * 100
            } else 0.0

            if (growthRate > 10) {
                insights.add(
                    BusinessInsight(
                        id = "strong_growth_${System.currentTimeMillis()}",
                        title = "Strong Business Growth",
                        description = "Business has grown by ${String.format("%.1f", growthRate)}% over the analyzed period.",
                        metric = "${String.format("%.1f", growthRate)}%",
                        trend = "up",
                        impact = "positive",
                        suggestion = "Consider expanding operations or investing in growth initiatives."
                    )
                )
            }
        }

        return insights
    }

    fun getTopCustomers(
        invoices: List<InvoiceEntity>,
        parties: List<PartyEntity>,
        limit: Int = 10
    ): List<TopCustomer> {
        val partyMap = parties.associateBy { it.id }
        val customerData = invoices
            .filter { it.partyId > 0 }
            .groupBy { it.partyId }
            .mapValues { (partyId, partyInvoices) ->
                val totalAmount = partyInvoices.sumOf { it.totalAmount }
                val invoiceCount = partyInvoices.size
                val averageOrderValue = if (invoiceCount > 0) totalAmount / invoiceCount else 0.0
                val partyName = partyMap[partyId]?.name ?: "Unknown"
                TopCustomer(
                    partyId = partyId,
                    partyName = partyName,
                    totalAmount = totalAmount,
                    invoiceCount = invoiceCount,
                    averageOrderValue = averageOrderValue
                )
            }
            .sortedByDescending { it.totalAmount }

        return customerData.take(limit)
    }

    fun getSalesTrends(
        invoices: List<InvoiceEntity>,
        months: Int = 6
    ): List<SalesTrend> {
        val calendar = Calendar.getInstance()
        val monthlyData = mutableMapOf<String, Double>()

        for (i in 0 until months) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val key = String.format("%d-%02d", year, month)
            monthlyData[key] = 0.0
        }

        invoices.forEach { invoice ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = invoice.invoiceDate
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val key = String.format("%d-%02d", year, month)
            if (monthlyData.containsKey(key)) {
                monthlyData[key] = monthlyData[key]!! + invoice.totalAmount
            }
        }

        val sortedEntries = monthlyData.entries.sortedBy { it.key }
        val trends = mutableListOf<SalesTrend>()

        sortedEntries.forEachIndexed { index, (period, amount) ->
            val changePercent = if (index > 0) {
                val previousAmount = sortedEntries[index - 1].value
                if (previousAmount > 0) {
                    ((amount - previousAmount) / previousAmount) * 100
                } else {
                    0.0
                }
            } else {
                0.0
            }
            trends.add(SalesTrend(period, amount, changePercent))
        }

        return trends
    }

    fun calculateMetrics(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        parties: List<PartyEntity>
    ): BusinessMetrics {
        val totalRevenue = invoices.sumOf { it.totalAmount }
        val totalExpenses = expenses.sumOf { it.amount }
        val profitMargin = if (totalRevenue > 0) {
            ((totalRevenue - totalExpenses) / totalRevenue) * 100
        } else 0.0

        val averageOrderValue = if (invoices.isNotEmpty()) {
            totalRevenue / invoices.size
        } else 0.0

        val uniqueParties = invoices.map { it.partyId }.distinct().size
        val totalTransactions = invoices.size
        val customerRetentionRate = if (totalTransactions > 0 && uniqueParties > 0) {
            val repeatCustomers = invoices.groupBy { it.partyId }
                .count { it.value.size > 1 }
            (repeatCustomers.toDouble() / uniqueParties) * 100
        } else 0.0

        val topCustomerConcentration = if (invoices.isNotEmpty() && uniqueParties > 0) {
            val partyTotals = invoices.groupBy { it.partyId }
                .mapValues { it.value.sumOf { inv -> inv.totalAmount } }
            val maxPartyAmount = partyTotals.values.maxOrNull() ?: 0.0
            if (totalRevenue > 0) (maxPartyAmount / totalRevenue) * 100 else 0.0
        } else 0.0

        return BusinessMetrics(
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            profitMargin = profitMargin,
            averageOrderValue = averageOrderValue,
            customerRetentionRate = customerRetentionRate,
            topCustomerConcentration = topCustomerConcentration
        )
    }
}