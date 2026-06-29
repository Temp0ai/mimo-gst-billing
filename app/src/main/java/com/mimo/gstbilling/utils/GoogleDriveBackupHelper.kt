package com.mimo.gstbilling.utils

import android.content.Context
import java.io.File

class GoogleDriveBackupHelper(context: Context) {

    fun getSignInIntent(): android.content.Intent {
        throw UnsupportedOperationException("Google Drive backup is not yet configured. Add play-services-drive dependency to enable.")
    }

    fun backupToDrive(account: Any, dbFile: File, callback: (Boolean) -> Unit) {
        callback(false)
    }
}
