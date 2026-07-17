package com.mimo.gstbilling.utils

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.File as JavaFile
import java.util.Collections

class GoogleDriveHelper(private val context: Context) {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    fun isSignedIn(): Boolean = getSignedInAccount() != null

    fun signOut(callback: (Boolean) -> Unit) {
        googleSignInClient.signOut()
            .addOnCompleteListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun backup(
        dbFile: JavaFile,
        callback: (Boolean, String?) -> Unit
    ) {
        val account = getSignedInAccount()
        if (account == null) {
            callback(false, "Not signed in")
            return
        }

        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Mimo GST Billing").build()

            val fileMetadata = File()
            fileMetadata.name = "mimo_gst_backup_${System.currentTimeMillis()}.db"
            fileMetadata.mimeType = "application/x-sqlite3"
            fileMetadata.parents = listOf("root")

            val mediaContent = FileContent("application/x-sqlite3", dbFile)
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent).execute()

            callback(true, uploadedFile.id)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, e.message)
        }
    }

    fun listBackups(
        callback: (Boolean, List<CloudBackupInfo>?) -> Unit
    ) {
        val account = getSignedInAccount()
        if (account == null) {
            callback(false, null)
            return
        }

        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Mimo GST Billing").build()

            val result: FileList = driveService.files().list()
                .setQ("name contains 'mimo_gst_backup_' and mimeType = 'application/x-sqlite3' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name, createdTime, size)")
                .setOrderBy("createdTime desc")
                .execute()

            val backups = result.files.map { file ->
                CloudBackupInfo(
                    fileId = file.id,
                    fileName = file.name,
                    createdTime = file.createdTime?.toString() ?: "Unknown",
                    size = file.getSize() ?: 0L
                )
            }

            callback(true, backups)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, null)
        }
    }

    fun restore(
        fileId: String,
        destinationFile: JavaFile,
        callback: (Boolean, String?) -> Unit
    ) {
        val account = getSignedInAccount()
        if (account == null) {
            callback(false, "Not signed in")
            return
        }

        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Mimo GST Billing").build()

            driveService.files().get(fileId)
                .executeMediaAndDownloadTo(destinationFile.outputStream())

            callback(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, e.message)
        }
    }

    data class CloudBackupInfo(
        val fileId: String,
        val fileName: String,
        val createdTime: String,
        val size: Long
    )
}