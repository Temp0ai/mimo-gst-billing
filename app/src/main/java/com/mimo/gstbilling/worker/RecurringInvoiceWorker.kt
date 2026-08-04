package com.mimo.gstbilling.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.InvoiceItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.dao.RecurringInvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringInvoiceWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringInvoiceDao: RecurringInvoiceDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val partyDao: PartyDao,
    private val companyDao: CompanyDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: 1L
            val now = System.currentTimeMillis()
            val dueRecurring = recurringInvoiceDao.getDueRecurring(companyId, now)

            for (recurring in dueRecurring) {
                // Generate invoice
                val invoiceNumber = generateInvoiceNumber(recurring.invoiceType)
                val invoice = InvoiceEntity(
                    companyId = companyId,
                    partyId = recurring.partyId,
                    partyName = recurring.partyName,
                    invoiceNumber = invoiceNumber,
                    invoiceDate = now,
                    totalAmount = recurring.amount,
                    taxableAmount = recurring.amount,
                    paymentStatus = "unpaid",
                    invoiceType = recurring.invoiceType
                )
                val invoiceId = invoiceDao.insert(invoice)

                // Update recurring invoice
                val nextDueDate = calculateNextDueDate(recurring.nextDueDate, recurring.frequency)
                recurringInvoiceDao.updateRecurring(
                    recurring.copy(
                        nextDueDate = nextDueDate,
                        lastGeneratedDate = now
                    )
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun generateInvoiceNumber(type: String): String {
        val prefix = if (type == "purchase") "PUR" else "INV"
        val timestamp = System.currentTimeMillis() % 100000
        return "$prefix-${String.format("%05d", timestamp)}"
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (frequency) {
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "quarterly" -> calendar.add(Calendar.MONTH, 3)
            "yearly" -> calendar.add(Calendar.YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val WORK_NAME = "recurring_invoice_worker"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<RecurringInvoiceWorker>(
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

        fun runNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<RecurringInvoiceWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
