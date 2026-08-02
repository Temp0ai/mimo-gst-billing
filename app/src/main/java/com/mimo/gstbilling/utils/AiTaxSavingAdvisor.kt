package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import java.util.Calendar

object AiTaxSavingAdvisor {

    data class TaxSuggestion(
        val id: String,
        val title: String,
        val description: String,
        val potentialSavings: Double,
        val category: String,
        val priority: String
    )

    data class TaxSummary(
        val totalTaxPaid: Double,
        val totalSavings: Double,
        val effectiveTaxRate: Double,
        val suggestions: List<TaxSuggestion>
    )

    fun analyzeTaxSavings(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        partyBalances: Map<Long, Double>
    ): TaxSummary {
        val gstSummary = calculateGstSummary(invoices)
        val itcSuggestions = suggestInputTaxCredit(expenses)
        val totalTaxPaid = gstSummary["netPayable"] ?: 0.0
        val totalSavings = itcSuggestions.sumOf { it.potentialSavings }
        val totalSales = invoices.sumOf { it.totalAmount }
        val effectiveTaxRate = if (totalSales > 0) (totalTaxPaid / totalSales) * 100 else 0.0

        val tcsTdsSuggestions = partyBalances.filter { it.value > 50000 }.map { (partyId, balance) ->
            TaxSuggestion(
                id = "TCS_${partyId}",
                title = "TCS Collection Opportunity",
                description = "Party balance of ₹${balance} exceeds threshold. Collect TCS at applicable rate.",
                potentialSavings = balance * 0.01,
                category = "TCS/TDS",
                priority = "Medium"
            )
        }

        val timingSuggestions = suggestExpenseTiming(
            Calendar.getInstance().get(Calendar.MONTH),
            groupExpensesByQuarter(expenses)
        )

        val allSuggestions = itcSuggestions + tcsTdsSuggestions + timingSuggestions

        return TaxSummary(
            totalTaxPaid = totalTaxPaid,
            totalSavings = totalSavings,
            effectiveTaxRate = effectiveTaxRate,
            suggestions = allSuggestions
        )
    }

    fun suggestInputTaxCredit(expenses: List<ExpenseEntity>): List<TaxSuggestion> {
        return expenses.filter { !it.itcClaimed && it.gstAmount > 0 }.map { expense ->
            TaxSuggestion(
                id = "ITC_${expense.id}",
                title = "Claim ITC on ${expense.category}",
                description = "₹${expense.gstAmount} ITC available on ${expense.category} expense dated ${expense.date}.",
                potentialSavings = expense.gstAmount,
                category = "Input Tax Credit",
                priority = if (expense.gstAmount > 5000) "High" else "Medium"
            )
        }
    }

    fun suggestExpenseTiming(
        currentMonth: Int,
        quarterlyExpenses: Map<Int, Double>
    ): List<TaxSuggestion> {
        val suggestions = mutableListOf<TaxSuggestion>()

        if (currentMonth >= 9) {
            val q3Expenses = quarterlyExpenses[3] ?: 0.0
            if (q3Expenses < 100000) {
                suggestions.add(
                    TaxSuggestion(
                        id = "TIMING_YEAR_END",
                        title = "Year-End Tax Planning",
                        description = "Consider accelerating eligible expenses before March 31 for current FY deduction.",
                        potentialSavings = 25000.0,
                        category = "Expense Timing",
                        priority = "High"
                    )
                )
            }
        }

        if (currentMonth in 0..2) {
            suggestions.add(
                TaxSuggestion(
                    id = "TIMING_Q4",
                    title = "Q4 Expense Optimization",
                    description = "Plan major purchases in Q4 (Jan-Mar) to maximize current year ITC claims.",
                    potentialSavings = 15000.0,
                    category = "Expense Timing",
                    priority = "Medium"
                )
            )
        }

        val lowestQuarter = quarterlyExpenses.minByOrNull { it.value }
        if (lowestQuarter != null && lowestQuarter.value < 50000) {
            suggestions.add(
                TaxSuggestion(
                    id = "TIMING_Q${lowestQuarter.key}",
                    title = "Increase Q${lowestQuarter.key} Expenses",
                    description = "Q${lowestQuarter.key} has low expenses (₹${lowestQuarter.value}). Consider scheduling purchases.",
                    potentialSavings = 10000.0,
                    category = "Expense Timing",
                    priority = "Low"
                )
            )
        }

        return suggestions
    }

    fun calculateGstSummary(invoices: List<InvoiceEntity>): Map<String, Double> {
        var cgst = 0.0
        var sgst = 0.0
        var igst = 0.0

        invoices.forEach { invoice ->
            if (invoice.igstAmount > 0) {
                igst += invoice.igstAmount
            } else {
                cgst += invoice.cgstAmount
                sgst += invoice.sgstAmount
            }
        }

        val total = cgst + sgst + igst
        val inputCredit = invoices.sumOf { it.inputTaxCredit }
        val netPayable = (total - inputCredit).coerceAtLeast(0.0)

        return mapOf(
            "cgst" to cgst,
            "sgst" to sgst,
            "igst" to igst,
            "total" to total,
            "inputCredit" to inputCredit,
            "netPayable" to netPayable
        )
    }

    private fun groupExpensesByQuarter(expenses: List<ExpenseEntity>): Map<Int, Double> {
        val quarterMap = mutableMapOf<Int, Double>()
        expenses.forEach { expense ->
            val cal = Calendar.getInstance().apply { timeInMillis = expense.date }
            val month = cal.get(Calendar.MONTH)
            val quarter = when (month) {
                in 0..2 -> 4
                in 3..5 -> 1
                in 6..8 -> 2
                else -> 3
            }
            quarterMap[quarter] = (quarterMap[quarter] ?: 0.0) + expense.amount
        }
        return quarterMap
    }
}
