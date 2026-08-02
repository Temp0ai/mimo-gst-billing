package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AiCashFlowForecaster {

    data class CashFlowForecast(
        val month: String,
        val projectedIncome: Double,
        val projectedExpenses: Double,
        val netCashFlow: Double,
        val cumulativeBalance: Double
    )

    data class CashFlowSummary(
        val currentBalance: Double,
        val monthlyAverageIncome: Double,
        val monthlyAverageExpenses: Double,
        val runwayMonths: Int,
        val trend: String
    )

    fun forecastCashFlow(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        months: Int
    ): List<CashFlowForecast> {
        val cal = Calendar.getInstance()
        val forecasts = mutableListOf<CashFlowForecast>()
        val historicalData = groupByMonth(invoices, expenses)

        if (historicalData.isEmpty()) {
            val currentMonth = Calendar.getInstance()
            repeat(months) {
                forecasts.add(
                    CashFlowForecast(
                        month = formatMonth(currentMonth),
                        projectedIncome = 0.0,
                        projectedExpenses = 0.0,
                        netCashFlow = 0.0,
                        cumulativeBalance = 0.0
                    )
                )
                currentMonth.add(Calendar.MONTH, 1)
            }
            return forecasts
        }

        val historicalMonths = historicalData.keys.sorted()
        val monthlyIncomes = historicalMonths.map { historicalData[it]?.first ?: 0.0 }
        val monthlyExpenses = historicalMonths.map { historicalData[it]?.second ?: 0.0 }

        val incomeTrend = calculateTrend(monthlyIncomes)
        val expenseTrend = calculateTrend(monthlyExpenses)

        val avgIncome = monthlyIncomes.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val avgExpense = monthlyExpenses.takeIf { it.isNotEmpty() }?.average() ?: 0.0

        val lastMonth = Calendar.getInstance()
        if (historicalMonths.isNotEmpty()) {
            lastMonth.timeInMillis = historicalMonths.last()
        }

        var cumulativeBalance = invoices.sumOf { it.totalAmount } - expenses.sumOf { it.amount }

        repeat(months) { i ->
            val futureMonth = Calendar.getInstance()
            futureMonth.timeInMillis = lastMonth.timeInMillis
            futureMonth.add(Calendar.MONTH, i + 1)

            val projectedIncome = avgIncome + (incomeTrend * (i + 1))
            val projectedExpense = avgExpense + (expenseTrend * (i + 1))

            val netCashFlow = projectedIncome - projectedExpense
            cumulativeBalance += netCashFlow

            forecasts.add(
                CashFlowForecast(
                    month = formatMonth(futureMonth),
                    projectedIncome = maxOf(0.0, projectedIncome),
                    projectedExpenses = maxOf(0.0, projectedExpense),
                    netCashFlow = netCashFlow,
                    cumulativeBalance = cumulativeBalance
                )
            )
        }

        return forecasts
    }

    fun generateSummary(
        invoices: List<InvoiceEntity>,
        currentBalance: Double
    ): CashFlowSummary {
        val monthlyData = groupByMonth(invoices, emptyList())
        val historicalMonths = monthlyData.keys.sorted()

        val monthlyIncomes = historicalMonths.map { monthlyData[it]?.first ?: 0.0 }
        val monthlyExpenses = historicalMonths.map { monthlyData[it]?.second ?: 0.0 }

        val avgIncome = monthlyIncomes.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val avgExpense = monthlyExpenses.takeIf { it.isNotEmpty() }?.average() ?: 0.0

        val netMonthly = avgIncome - avgExpense
        val runway = if (netMonthly <= 0 && currentBalance > 0) {
            (currentBalance / maxOf(1.0, avgExpense)).toInt()
        } else if (netMonthly > 0) {
            Int.MAX_VALUE
        } else {
            0
        }

        val trend = when {
            monthlyIncomes.size < 2 -> "insufficient_data"
            calculateTrend(monthlyIncomes) > 0 -> "increasing"
            calculateTrend(monthlyIncomes) < 0 -> "decreasing"
            else -> "stable"
        }

        return CashFlowSummary(
            currentBalance = currentBalance,
            monthlyAverageIncome = avgIncome,
            monthlyAverageExpenses = avgExpense,
            runwayMonths = runway,
            trend = trend
        )
    }

    fun detectSeasonalPatterns(invoices: List<InvoiceEntity>): Map<Int, Double> {
        val monthTotals = mutableMapOf<Int, MutableList<Double>>()

        invoices.forEach { invoice ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = invoice.invoiceDate
            val month = cal.get(Calendar.MONTH)
            monthTotals.getOrPut(month) { mutableListOf() }.add(invoice.totalAmount)
        }

        return monthTotals.mapValues { it.value.average() }
    }

    private fun groupByMonth(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>
    ): Map<Long, Pair<Double, Double>> {
        val data = mutableMapOf<Long, Pair<Double, Double>>()

        invoices.forEach { invoice ->
            val monthStart = getMonthStart(invoice.invoiceDate)
            val current = data[monthStart] ?: Pair(0.0, 0.0)
            data[monthStart] = current.copy(first = current.first + invoice.totalAmount)
        }

        expenses.forEach { expense ->
            val monthStart = getMonthStart(expense.date)
            val current = data[monthStart] ?: Pair(0.0, 0.0)
            data[monthStart] = current.copy(second = current.second + expense.amount)
        }

        return data
    }

    private fun getMonthStart(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatMonth(cal: Calendar): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
    }

    private fun calculateTrend(values: List<Double>): Double {
        if (values.size < 2) return 0.0

        val n = values.size.toDouble()
        val xValues = (0 until values.size).map { it.toDouble() }
        val yValues = values

        val sumX = xValues.sum()
        val sumY = yValues.sum()
        val sumXY = xValues.zip(yValues).sumOf { it.first * it.second }
        val sumX2 = xValues.sumOf { it * it }

        val denominator = (n * sumX2 - sumX * sumX)
        if (denominator == 0.0) return 0.0

        return (n * sumXY - sumX * sumY) / denominator
    }
}
