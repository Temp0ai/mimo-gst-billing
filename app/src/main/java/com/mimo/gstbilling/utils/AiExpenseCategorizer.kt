package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import java.util.Calendar

/**
 * AI Expense Categorizer - Automatically categorizes expenses using keyword matching,
 * amount patterns, and historical data analysis.
 */
object AiExpenseCategorizer {

    private val categoryKeywords = mapOf(
        "Rent" to listOf("rent", "lease", "office space", "warehouse rent", "shop rent"),
        "Utilities" to listOf("electricity", "water", "gas", "internet", "phone", "broadband", "power"),
        "Salary" to listOf("salary", "wages", "payroll", "employee", "staff", "bonus", "incentive"),
        "Raw Material" to listOf("raw material", "component", "parts", "input material", "procurement"),
        "Transport" to listOf("transport", "freight", "courier", "delivery", "logistics", "shipping"),
        "Marketing" to listOf("marketing", "advertisement", "promotion", "ads", "campaign", "branding"),
        "Office Supplies" to listOf("office", "stationery", "printer", "paper", "ink", "supplies"),
        "Maintenance" to listOf("maintenance", "repair", "service", "amc", "cleaning"),
        "Insurance" to listOf("insurance", "premium", "policy", "coverage"),
        "Legal" to listOf("legal", "ca fee", "audit", "compliance", "registration", "stamp duty"),
        "Travel" to listOf("travel", "hotel", "flight", "taxi", "fuel", "petrol", "diesel"),
        "Telecom" to listOf("mobile", "telephone", "sim", "recharge", "broadband"),
        "Software" to listOf("software", "subscription", "saas", "cloud", "hosting", "domain"),
        "Miscellaneous" to listOf("misc", "other", "general", "petty cash")
    )

    private val amountPatterns = mapOf(
        "Rent" to 10000.0..500000.0,
        "Salary" to 15000.0..1000000.0,
        "Utilities" to 500.0..50000.0,
        "Insurance" to 1000.0..200000.0,
        "Legal" to 2000.0..500000.0
    )

    data class CategoryResult(
        val category: String,
        val confidence: Float, // 0.0 to 1.0
        val alternativeCategories: List<Pair<String, Float>>
    )

    /**
     * Categorize an expense based on description and amount
     */
    fun categorize(description: String, amount: Double): CategoryResult {
        val normalizedDesc = description.lowercase().trim()
        val scores = mutableMapOf<String, Float>()

        // Keyword matching (60% weight)
        for ((category, keywords) in categoryKeywords) {
            var matchCount = 0
            for (keyword in keywords) {
                if (normalizedDesc.contains(keyword)) {
                    matchCount++
                }
            }
            if (matchCount > 0) {
                scores[category] = (matchCount.toFloat() / keywords.size) * 0.6f
            }
        }

        // Amount pattern matching (25% weight)
        for ((category, range) in amountPatterns) {
            if (amount in range) {
                scores[category] = (scores[category] ?: 0f) + 0.25f
            }
        }

        // Length and pattern heuristics (15% weight)
        if (normalizedDesc.length < 5) {
            scores["Miscellaneous"] = (scores["Miscellaneous"] ?: 0f) + 0.15f
        }

        if (scores.isEmpty()) {
            return CategoryResult("Miscellaneous", 0.3f, emptyList())
        }

        val sortedScores = scores.entries.sortedByDescending { it.value }
        val topCategory = sortedScores.first()
        val alternatives = sortedScores.drop(1).take(3).map { it.key to it.value }

        return CategoryResult(
            category = topCategory.key,
            confidence = minOf(topCategory.value, 1.0f),
            alternativeCategories = alternatives
        )
    }

    /**
     * Batch categorize multiple expenses
     */
    fun categorizeBatch(expenses: List<Pair<String, Double>>): List<CategoryResult> {
        return expenses.map { (desc, amount) -> categorize(desc, amount) }
    }

    /**
     * Learn from historical expenses to improve categorization
     */
    fun learnFromHistory(
        historicalExpenses: List<ExpenseEntity>
    ): Map<String, Map<String, Int>> {
        val categoryPatterns = mutableMapOf<String, MutableMap<String, Int>>()

        for (expense in historicalExpenses) {
            val words = expense.description.lowercase().split(" ", ",", ".", "-")
            val category = expense.category

            if (!categoryPatterns.containsKey(category)) {
                categoryPatterns[category] = mutableMapOf()
            }

            for (word in words) {
                if (word.length > 2) {
                    categoryPatterns[category]!![word] =
                        (categoryPatterns[category]!![word] ?: 0) + 1
                }
            }
        }

        return categoryPatterns
    }

    /**
     * Suggest category based on amount range analysis
     */
    fun suggestByAmountRange(amount: Double): List<String> {
        return amountPatterns.filter { it.value.contains(amount) }
            .keys
            .toList()
            .ifEmpty { listOf("Miscellaneous") }
    }
}
