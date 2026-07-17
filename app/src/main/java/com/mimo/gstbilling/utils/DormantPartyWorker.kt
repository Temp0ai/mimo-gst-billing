package com.mimo.gstbilling.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DormantPartyWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val companyId = 1L
                val parties = partyDao.getPartiesByCompany(companyId)
                val invoices = invoiceDao.getInvoicesByCompany(companyId)
                val now = System.currentTimeMillis()
                val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

                DormantPartyNotifier.createNotificationChannel(context)

                var notified = 0
                for (party in parties) {
                    val partyInvoices = invoices.filter { it.partyId == party.id }
                    if (partyInvoices.isEmpty()) continue

                    val lastOrderDate = partyInvoices.maxOfOrNull { it.invoiceDate } ?: continue
                    val daysSince = ((now - lastOrderDate) / (24 * 60 * 60 * 1000)).toInt()

                    if (daysSince >= 30) {
                        DormantPartyNotifier.showNotification(context, party.name, daysSince)
                        notified++
                        if (notified >= 5) break
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
}
