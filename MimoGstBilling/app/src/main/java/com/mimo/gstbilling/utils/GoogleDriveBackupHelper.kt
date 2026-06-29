package com.mimo.gstbilling.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.util.Collections

class GoogleDriveBackupHelper(context: Context) {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(com.google.android.gms.common.Scopes.DRIVE_FILE)
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun backupToDrive(account: GoogleSignInAccount, dbFile: File, callback: (Boolean) -> Unit) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                account.applicationContext,
                Collections.singleton(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Mimo GST Billing").build()

            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = "mimo_gst_backup_${System.currentTimeMillis()}.db"
            fileMetadata.mimeType = "application/x-sqlite3"
            fileMetadata.parents = listOf("root")

            val mediaContent = FileContent("application/x-sqlite3", dbFile)
            driveService.files().create(fileMetadata, mediaContent).execute()
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }
}
