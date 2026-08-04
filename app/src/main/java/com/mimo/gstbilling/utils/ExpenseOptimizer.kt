package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.ExpenseEntity

object ExpenseOptimizer {

    data class ExpenseSuggestion(
        val category: String,
        val currentMonthly: Double,
        val avgMonthly: Double,
        val savingPotential: Double,
        val suggestion: String,
        val priority: String
    )

    fun analyze(expenses: List<ExpenseEntity>): List<ExpenseSuggestion> {
        if (expenses.isEmpty()) return emptyList()

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        val monthlyExpenses = expenses.groupBy { dateFormat.format(java.util.Date(it.date)) }
        if (monthlyExpenses.size < 2) return emptyList()

        val categoryMonthly = mutableMapOf<String, MutableList<Double>>()

        for ((_, monthExpenses) in monthlyExpenses) {
            val byCategory = monthExpenses.groupBy { it.category }
            for ((cat, catExpenses) in byCategory) {
                categoryMonthly.getOrPut(cat) { mutableListOf() }.add(catExpenses.sumOf { it.amount })
            }
        }

        val suggestions = mutableListOf<ExpenseSuggestion>()

        for ((category, monthlyAmounts) in categoryMonthly) {
            val avg = monthlyAmounts.average()
            val current = monthlyAmounts.lastOrNull() ?: 0.0
            val max = monthlyAmounts.maxOrNull() ?: 0.0
            val min = monthlyAmounts.minOrNull() ?: 0.0

            if (current > avg * 1.2 && monthlyAmounts.size >= 2) {
                val savingPotential = current - avg
                val priority = when {
                    savingPotential > avg * 0.5 -> "HIGH"
                    savingPotential > avg * 0.2 -> "MEDIUM"
                    else -> "LOW"
                }
                suggestions.add(ExpenseSuggestion(
                    category = category,
                    currentMonthly = String.format("%.0f", current).toDouble(),
                    avgMonthly = String.format("%.0f", avg).toDouble(),
                    savingPotential = String.format("%.0f", savingPotential).toDouble(),
                    suggestion = "$category is ${String.format("%.0f", ((current / avg - 1) * 100))}% above average",
                    priority = priority
                ))
            }
        }

        val totalCurrent = expenses.filter { dateFormat.format(java.util.Date(it.date)) == monthlyExpenses.keys.lastOrNull() }.sumOf { it.amount }
        val totalAvg = monthlyExpenses.values.map { it.sumOf { e -> e.amount } }.average()

        if (totalCurrent > totalAvg * 1.15) {
            suggestions.add(ExpenseSuggestion(
                category = "OVERALL",
                currentMonthly = String.format("%.0f", totalCurrent).toDouble(),
                avgMonthly = String.format("%.0f", totalAvg).toDouble(),
                savingPotential = String.format("%.0f", totalCurrent - totalAvg).toDouble(),
                suggestion = "Total expenses above average - review all categories",
                priority = "HIGH"
            ))
        }

        return suggestions.sortedByDescending { it.savingPotential }.take(10)
    }
}
