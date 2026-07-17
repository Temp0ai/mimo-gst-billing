package com.mimo.gstbilling.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.mimo.gstbilling.MainActivity
import com.mimo.gstbilling.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DormantPartyNotifier {

    companion object {
        private const val CHANNEL_ID = "dormant_party_alerts"
        private const val CHANNEL_NAME = "Inactive Party Alerts"
        private const val NOTIFICATION_TAG = "dormant_parties"
        private const val WORK_TAG = "dormant_party_check"

        fun scheduleDailyCheck(context: Context) {
            val request = PeriodicWorkRequestBuilder<DormantPartyWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(1, TimeUnit.HOURS)
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
                request
            )
        }

        fun cancelDailyCheck(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for parties who haven't ordered in 30+ days"
                    enableLights(true)
                    enableVibration(true)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

        fun showNotification(context: Context, partyName: String, daysSince: Int) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val manager = context.getSystemService(NotificationManager::class.java)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Inactive Party: $partyName")
                .setContentText("No orders in $daysSince days. Consider reaching out!")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Party $partyName hasn't placed any orders in $daysSince days. You may want to send a reminder or check in with them."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(NOTIFICATION_TAG)
                .build()

            manager.notify("${NOTIFICATION_TAG}_${partyName.hashCode()}", 0, notification)
        }
    }
}
