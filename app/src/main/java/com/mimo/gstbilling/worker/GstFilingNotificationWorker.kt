package com.mimo.gstbilling.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.mimo.gstbilling.utils.GstFilingReminder
import java.util.concurrent.TimeUnit

class GstFilingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "gst_filing_channel"
        private const val WORK_TAG = "gst_filing_notifications"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<GstFilingNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()
        checkAndNotify()
        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GST Filing Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for upcoming GST filing deadlines"
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun checkAndNotify() {
        val deadlines = GstFilingReminder.getUpcoming()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        deadlines.forEachIndexed { index, deadline ->
            if (deadline.daysRemaining <= 7) {
                val priority = when {
                    deadline.daysRemaining <= 3 -> NotificationCompat.PRIORITY_HIGH
                    deadline.daysRemaining <= 5 -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_LOW
                }

                val title = when {
                    deadline.daysRemaining <= 0 -> "${deadline.returnType} Filing OVERDUE!"
                    deadline.daysRemaining <= 3 -> "URGENT: ${deadline.returnType} due in ${deadline.daysRemaining} days"
                    else -> "${deadline.returnType} due in ${deadline.daysRemaining} days"
                }

                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText("${deadline.description} | Due: ${deadline.dueDate} | Period: ${deadline.period}")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(
                        "${deadline.description}\nDue Date: ${deadline.dueDate}\nPeriod: ${deadline.period}\nStatus: ${deadline.status}\n\nFile your ${deadline.returnType} on time to avoid penalties."
                    ))
                    .setPriority(priority)
                    .setAutoCancel(true)
                    .build()

                manager.notify(2000 + index, notification)
            }
        }
    }
}
