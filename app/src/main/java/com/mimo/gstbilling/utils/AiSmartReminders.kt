package com.mimo.gstbilling.utils

import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AiSmartReminders {

    data class SmartReminder(
        val id: String,
        val partyId: Long,
        val partyName: String,
        val invoiceId: Long,
        val invoiceNumber: String,
        val amount: Double,
        val daysOverdue: Int,
        val reminderType: String,
        val suggestedMessage: String,
        val priority: Int
    )

    fun generateReminders(
        invoices: List<InvoiceEntity>,
        parties: List<PartyEntity>
    ): List<SmartReminder> {
        val partyMap = parties.associateBy { it.id }
        val currentDate = Date()
        val reminders = mutableListOf<SmartReminder>()

        for (invoice in invoices) {
            if (invoice.isPaid) continue

            val party = partyMap[invoice.partyId] ?: continue
            val daysOverdue = calculateDaysOverdue(invoice, currentDate)

            val reminderType = when {
                daysOverdue <= 15 -> "gentle"
                daysOverdue <= 30 -> "firm"
                daysOverdue <= 60 -> "urgent"
                else -> "final"
            }

            val message = generateReminderMessage(party, invoice, daysOverdue)
            val priority = calculatePriority(invoice.amount, daysOverdue)

            reminders.add(
                SmartReminder(
                    id = "REM_${invoice.id}_${System.currentTimeMillis()}",
                    partyId = party.id,
                    partyName = party.name,
                    invoiceId = invoice.id,
                    invoiceNumber = invoice.invoiceNumber,
                    amount = invoice.amount,
                    daysOverdue = daysOverdue,
                    reminderType = reminderType,
                    suggestedMessage = message,
                    priority = priority
                )
            )
        }

        return prioritizeReminders(reminders)
    }

    fun generateReminderMessage(
        party: PartyEntity,
        invoice: InvoiceEntity,
        daysOverdue: Int
    ): String {
        val partyName = party.name
        val invoiceNumber = invoice.invoiceNumber
        val amount = String.format("%.2f", invoice.amount)

        return when {
            daysOverdue <= 15 -> {
                "Dear $partyName, this is a friendly reminder that invoice #$invoiceNumber for ₹$amount is now $daysOverdue days overdue. " +
                "Please let us know if you need any assistance with the payment. We appreciate your business."
            }
            daysOverdue <= 30 -> {
                "Hello $partyName, we're following up on invoice #$invoiceNumber for ₹$amount which is $daysOverdue days overdue. " +
                "Kindly arrange the payment at your earliest convenience to avoid any inconvenience."
            }
            daysOverdue <= 60 -> {
                "Attention $partyName, invoice #$invoiceNumber for ₹$amount is now $daysOverdue days past due. " +
                "We urge you to settle this outstanding amount immediately to prevent further action."
            }
            else -> {
                "URGENT: $partyName, your invoice #$invoiceNumber for ₹$amount is severely overdue at $daysOverdue days. " +
                "Immediate payment is required to avoid account suspension and legal proceedings. Contact us immediately."
            }
        }
    }

    fun getReminderStats(reminders: List<SmartReminder>): Map<String, Int> {
        if (reminders.isEmpty()) {
            return mapOf(
                "gentle" to 0,
                "firm" to 0,
                "urgent" to 0,
                "final" to 0,
                "totalReminders" to 0,
                "totalOverdueAmount" to 0,
                "averageDaysOverdue" to 0
            )
        }

        val gentleCount = reminders.count { it.reminderType == "gentle" }
        val firmCount = reminders.count { it.reminderType == "firm" }
        val urgentCount = reminders.count { it.reminderType == "urgent" }
        val finalCount = reminders.count { it.reminderType == "final" }
        val totalAmount = reminders.sumOf { it.amount }.toInt()
        val averageDays = reminders.map { it.daysOverdue }.average().toInt()

        return mapOf(
            "gentle" to gentleCount,
            "firm" to firmCount,
            "urgent" to urgentCount,
            "final" to finalCount,
            "totalReminders" to reminders.size,
            "totalOverdueAmount" to totalAmount,
            "averageDaysOverdue" to averageDays
        )
    }

    fun prioritizeReminders(reminders: List<SmartReminder>): List<SmartReminder> {
        return reminders.sortedByDescending { it.priority }
    }

    private fun calculateDaysOverdue(invoice: InvoiceEntity, currentDate: Date): Int {
        val dueDate = invoice.dueDate
        val diffMillis = currentDate.time - dueDate
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(0)
    }

    private fun calculatePriority(amount: Double, daysOverdue: Int): Int {
        val amountScore = when {
            amount >= 100000 -> 5
            amount >= 50000 -> 4
            amount >= 10000 -> 3
            amount >= 5000 -> 2
            else -> 1
        }

        val daysScore = when {
            daysOverdue >= 60 -> 5
            daysOverdue >= 30 -> 4
            daysOverdue >= 15 -> 3
            daysOverdue >= 5 -> 2
            else -> 1
        }

        return ((amountScore + daysScore) / 2).coerceIn(1, 5)
    }
}
