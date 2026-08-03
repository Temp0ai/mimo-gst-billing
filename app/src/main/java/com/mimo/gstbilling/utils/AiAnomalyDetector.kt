package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

object AiAnomalyDetector {

    data class Anomaly(
        val id: String,
        val type: String,
        val severity: String,
        val description: String,
        val entityId: Long,
        val entityType: String,
        val detectedValue: Double,
        val expectedRange: String
    )

    data class AnomalyReport(
        val anomalies: List<Anomaly>,
        val totalChecked: Int,
        val anomalyCount: Int,
        val riskScore: Float
    )

    fun detectInvoiceAnomalies(invoices: List<InvoiceEntity>): List<Anomaly> {
        if (invoices.size < 2) return emptyList()

        val anomalies = mutableListOf<Anomaly>()
        val amounts = invoices.map { it.totalAmount }
        val mean = amounts.average()
        val variance = amounts.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        invoices.forEach { invoice ->
            val amount = invoice.totalAmount
            if (amount > mean + 2 * stdDev) {
                anomalies.add(
                    Anomaly(
                        id = "INV-${invoice.id}-HIGH",
                        type = "high_amount",
                        severity = "high",
                        description = "Invoice amount significantly above average",
                        entityId = invoice.id,
                        entityType = "invoice",
                        detectedValue = amount,
                        expectedRange = "0 - %.2f".format(mean + 2 * stdDev)
                    )
                )
            }
            if (stdDev > 0 && amount < mean - 2 * stdDev) {
                anomalies.add(
                    Anomaly(
                        id = "INV-${invoice.id}-LOW",
                        type = "low_amount",
                        severity = "medium",
                        description = "Invoice amount significantly below average",
                        entityId = invoice.id,
                        entityType = "invoice",
                        detectedValue = amount,
                        expectedRange = "%.2f - %.2f".format(mean - 2 * stdDev, mean + 2 * stdDev)
                    )
                )
            }
            if (amount > 0 && amount % 1000.0 == 0.0) {
                anomalies.add(
                    Anomaly(
                        id = "INV-${invoice.id}-ROUND",
                        type = "round_number",
                        severity = "low",
                        description = "Invoice amount is suspiciously round number",
                        entityId = invoice.id,
                        entityType = "invoice",
                        detectedValue = amount,
                        expectedRange = "Non-round amounts expected"
                    )
                )
            }
            val cal = Calendar.getInstance()
            cal.timeInMillis = invoice.invoiceDate
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                anomalies.add(
                    Anomaly(
                        id = "INV-${invoice.id}-WEEKEND",
                        type = "weekend_invoice",
                        severity = "low",
                        description = "Invoice created on weekend",
                        entityId = invoice.id,
                        entityType = "invoice",
                        detectedValue = 0.0,
                        expectedRange = "Weekdays only"
                    )
                )
            }
        }

        val sortedInvoices = invoices.sortedBy { it.invoiceNumber }
        for (i in 1 until sortedInvoices.size) {
            val currentNum = extractInvoiceNumber(sortedInvoices[i].invoiceNumber)
            val previousNum = extractInvoiceNumber(sortedInvoices[i - 1].invoiceNumber)
            if (currentNum != -1L && previousNum != -1L && currentNum - previousNum > 1) {
                anomalies.add(
                    Anomaly(
                        id = "INV-GAP-${sortedInvoices[i - 1].id}-${sortedInvoices[i].id}",
                        type = "sequential_gap",
                        severity = "medium",
                        description = "Invoice number gap detected",
                        entityId = sortedInvoices[i - 1].id,
                        entityType = "invoice",
                        detectedValue = (currentNum - previousNum).toDouble(),
                        expectedRange = "Sequential numbers"
                    )
                )
            }
        }

        return anomalies
    }

    fun detectExpenseAnomalies(expenses: List<ExpenseEntity>): List<Anomaly> {
        if (expenses.size < 2) return emptyList()

        val anomalies = mutableListOf<Anomaly>()
        val amounts = expenses.map { it.amount }
        val mean = amounts.average()
        val variance = amounts.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        expenses.forEach { expense ->
            if (expense.amount > mean + 2 * stdDev) {
                anomalies.add(
                    Anomaly(
                        id = "EXP-${expense.id}-LARGE",
                        type = "large_expense",
                        severity = "high",
                        description = "Expense significantly above average",
                        entityId = expense.id,
                        entityType = "expense",
                        detectedValue = expense.amount,
                        expectedRange = "0 - %.2f".format(mean + 2 * stdDev)
                    )
                )
            }
        }

        val groupedByDate = expenses.groupBy { it.date }
        groupedByDate.values.filter { it.size > 1 }.forEach { duplicateGroup ->
            anomalies.add(
                Anomaly(
                    id = "EXP-DUP-${duplicateGroup.first().id}",
                    type = "duplicate_time",
                    severity = "medium",
                    description = "Multiple expenses at same time",
                    entityId = duplicateGroup.first().id,
                    entityType = "expense",
                    detectedValue = duplicateGroup.size.toDouble(),
                    expectedRange = "1 expense per timestamp"
                )
            )
        }

        val categoryExpenses = expenses.groupBy { it.category }
        categoryExpenses.forEach { (category, catExpenses) ->
            val catMean = catExpenses.map { it.amount }.average()
            catExpenses.filter { it.amount > catMean * 3 }.forEach { expense ->
                anomalies.add(
                    Anomaly(
                        id = "EXP-${expense.id}-CAT",
                        type = "category_excess",
                        severity = "medium",
                        description = "Expense exceeds category average",
                        entityId = expense.id,
                        entityType = "expense",
                        detectedValue = expense.amount,
                        expectedRange = "Category avg: %.2f".format(catMean)
                    )
                )
            }
        }

        return anomalies
    }

    fun detectPartyAnomalies(
        parties: List<PartyEntity>,
        invoices: List<InvoiceEntity>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        val partyInvoices = invoices.groupBy { it.partyId }
        partyInvoices.forEach { (partyId, partyInvList) ->
            if (partyInvList.size < 3) return@forEach

            val amounts = partyInvList.map { it.totalAmount }
            val mean = amounts.average()
            val variance = amounts.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)

            partyInvList.forEach { invoice ->
                if (invoice.totalAmount > mean + 2 * stdDev) {
                    anomalies.add(
                        Anomaly(
                            id = "PARTY-${partyId}-SPIKE",
                            type = "sudden_large_order",
                            severity = "high",
                            description = "Sudden large order for party",
                            entityId = partyId,
                            entityType = "party",
                            detectedValue = invoice.totalAmount,
                            expectedRange = "0 - %.2f".format(mean + 2 * stdDev)
                        )
                    )
                }
            }
        }

        val partyPayments = invoices.groupBy { it.partyId }
            .mapValues { entry -> entry.value.map { it.paymentStatus } }
        partyPayments.forEach { (partyId, statuses) ->
            val paidCount = statuses.count { it == "paid" }
            val totalCount = statuses.size
            if (totalCount > 3 && paidCount.toDouble() / totalCount < 0.3) {
                anomalies.add(
                    Anomaly(
                        id = "PARTY-${partyId}-PAYMENT",
                        type = "unusual_payment_pattern",
                        severity = "medium",
                        description = "Party has mostly unpaid invoices",
                        entityId = partyId,
                        entityType = "party",
                        detectedValue = paidCount.toDouble(),
                        expectedRange = "At least 50% paid"
                    )
                )
            }
        }

        parties.forEach { party ->
            val gstin = party.gstin
            if (!gstin.isNullOrBlank() && !isValidGstin(gstin)) {
                anomalies.add(
                    Anomaly(
                        id = "PARTY-${party.id}-GSTIN",
                        type = "invalid_gstin",
                        severity = "high",
                        description = "Invalid GSTIN format",
                        entityId = party.id,
                        entityType = "party",
                        detectedValue = 0.0,
                        expectedRange = "Valid 15-char GSTIN"
                    )
                )
            }
        }

        return anomalies
    }

    fun generateReport(
        invoices: List<InvoiceEntity>,
        expenses: List<ExpenseEntity>,
        parties: List<PartyEntity>
    ): AnomalyReport {
        val invoiceAnomalies = detectInvoiceAnomalies(invoices)
        val expenseAnomalies = detectExpenseAnomalies(expenses)
        val partyAnomalies = detectPartyAnomalies(parties, invoices)

        val allAnomalies = invoiceAnomalies + expenseAnomalies + partyAnomalies
        val totalChecked = invoices.size + expenses.size + parties.size
        val anomalyCount = allAnomalies.size

        val highCount = allAnomalies.count { it.severity == "high" }
        val mediumCount = allAnomalies.count { it.severity == "medium" }
        val lowCount = allAnomalies.count { it.severity == "low" }

        val riskScore = if (totalChecked > 0) {
            ((highCount * 3 + mediumCount * 2 + lowCount * 1).toFloat() / totalChecked * 100).coerceIn(0f, 100f)
        } else {
            0f
        }

        return AnomalyReport(
            anomalies = allAnomalies,
            totalChecked = totalChecked,
            anomalyCount = anomalyCount,
            riskScore = riskScore
        )
    }

    private fun extractInvoiceNumber(invoiceNumber: String): Long {
        return try {
            invoiceNumber.filter { it.isDigit() }.toLongOrNull() ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    private fun isValidGstin(gstin: String): Boolean {
        if (gstin.length != 15) return false
        val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
        return gstinRegex.matches(gstin)
    }
}
