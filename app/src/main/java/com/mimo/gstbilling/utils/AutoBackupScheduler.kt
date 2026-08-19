package com.mimo.gstbilling.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AutoBackupScheduler {

    companion object {
        private const val PREFS_NAME = "mimo_prefs"
        private const val KEY_AUTO_BACKUP_FREQUENCY = "auto_backup_frequency"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val BACKUP_WORK_TAG = "auto_backup_work"
        private const val MAX_BACKUPS = 10

        fun scheduleAutoBackup(context: Context, frequency: String) {
            val prefs = getPrefs(context)
            prefs.edit().putString(KEY_AUTO_BACKUP_FREQUENCY, frequency).apply()

            cancelAutoBackup(context)

            if (frequency == "disabled") return

            val interval = when (frequency) {
                "daily" -> 1L
                "weekly" -> 7L
                "monthly" -> 30L
                else -> return
            }

            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                interval, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag(BACKUP_WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BACKUP_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancelAutoBackup(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(BACKUP_WORK_TAG)
        }

        fun getCurrentFrequency(context: Context): String {
            return getPrefs(context).getString(KEY_AUTO_BACKUP_FREQUENCY, "disabled") ?: "disabled"
        }

        fun getNextBackupTime(context: Context): String {
            val frequency = getCurrentFrequency(context)
            if (frequency == "disabled") return "Automatic backup is disabled"

            val prefs = getPrefs(context)
            val lastBackup = prefs.getString(KEY_LAST_BACKUP, null)

            val calendar = Calendar.getInstance()
            if (lastBackup != null) {
                try {
                    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
                    val date = sdf.parse(lastBackup)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }

            when (frequency) {
                "daily" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "monthly" -> calendar.add(Calendar.MONTH, 1)
            }

            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
            return "Next backup: ${sdf.format(calendar.time)}"
        }

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dbFile = applicationContext.getDatabasePath("mimo_gst_billing_db")
            if (!dbFile.exists()) {
                return@withContext Result.failure()
            }

            val backupDir = File(applicationContext.filesDir, "backups")
            backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.db")

            dbFile.copyTo(backupFile, overwrite = true)

            cleanupOldBackups(backupDir)

            val prefs = applicationContext.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
            val now = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())
            prefs.edit().putString("last_backup", now).apply()

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun cleanupOldBackups(backupDir: File) {
        val backups = backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") && it.extension == "db" }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (backups.size > 10) {
            backups.drop(10).forEach { it.delete() }
        }
    }

        fun autoBackupOnClose(context: Context) {
            val prefs = getPrefs(context)
            val frequency = prefs.getString(KEY_AUTO_BACKUP_FREQUENCY, "disabled") ?: "disabled"
            if (frequency == "disabled") return

            try {
                val dbFile = context.getDatabasePath("mimo_gst_billing_db")
                val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFile = File(backupDir, "backup_$timestamp.db")
                dbFile.copyTo(backupFile, overwrite = true)
                cleanupOldBackups(backupDir)
                val now = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())
                prefs.edit().putString(KEY_LAST_BACKUP, now).apply()
            } catch (_: Exception) {}
        }
    }
}