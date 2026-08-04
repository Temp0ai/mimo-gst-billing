package com.mimo.gstbilling.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class WhatsAppReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val companyDao: CompanyDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: 1L
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            // Get unpaid invoices older than 7 days
            val unpaidInvoices = invoiceDao.getUnpaidInvoices(companyId).first()

            for (invoice in unpaidInvoices) {
                val invoiceAge = now - invoice.invoiceDate
                if (invoiceAge >= 7L * 24 * 60 * 60 * 1000) {
                    val party = partyDao.getPartyById(invoice.partyId)
                    if (party != null && !party.phone.isNullOrBlank()) {
                        val daysOverdue = (invoiceAge / (24 * 60 * 60 * 1000)).toInt()
                        val message = generateReminderMessage(invoice.invoiceNumber, invoice.totalAmount, daysOverdue)
                        sendNotification(
                            "Payment Reminder: ${invoice.invoiceNumber}",
                            "Remind ${party.name} to pay \u20B9${String.format("%,.0f", invoice.totalAmount)} (${daysOverdue} days overdue)",
                            invoice.id,
                            party.phone,
                            message
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun generateReminderMessage(invoiceNumber: String, amount: Double, daysOverdue: Int): String {
        val greeting = when {
            daysOverdue <= 15 -> "Dear Customer, this is a gentle reminder"
            daysOverdue <= 30 -> "Dear Customer, this is a follow-up reminder"
            daysOverdue <= 60 -> "Dear Customer, your payment is significantly overdue"
            else -> "URGENT: Your payment is severely overdue"
        }
        return "$greeting. Invoice $invoiceNumber for \u20B9${String.format("%,.0f", amount)} is $daysOverdue days overdue. Please make the payment at the earliest. Thank you!"
    }

    private fun sendNotification(title: String, message: String, invoiceId: Long, phone: String, whatsappMessage: String) {
        createNotificationChannel()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encodedMessage = Uri.encode(whatsappMessage)
            data = Uri.parse("https://wa.me/91${phone}?text=$encodedMessage")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, invoiceId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val launchPendingIntent = PendingIntent.getActivity(
            context, invoiceId.toInt() + 10000, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(launchPendingIntent)
            .addAction(android.R.drawable.ic_dialog_info, "Send WhatsApp", pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(invoiceId.toInt(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Payment Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Auto payment reminder notifications"
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "payment_reminders"
        private const val WORK_NAME = "whatsapp_reminder_worker"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WhatsAppReminderWorker>(
                1, TimeUnit.DAYS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
