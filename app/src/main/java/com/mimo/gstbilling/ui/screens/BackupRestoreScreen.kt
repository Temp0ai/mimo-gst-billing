package com.mimo.gstbilling.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.LightBlueBg
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.utils.AutoBackupScheduler
import com.mimo.gstbilling.utils.GoogleDriveHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(navController: NavController) {
    val context = LocalContext.current
    val dbFile = context.getDatabasePath("mimo_gst_billing_db")
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US) }
    var lastBackup by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var backupListVersion by remember { mutableIntStateOf(0) }

    val backupDir = remember { File(context.filesDir, "backups").apply { mkdirs() } }
    val backups = remember(backupListVersion) { backupDir.listFiles()?.filter { it.extension == "db" }?.sortedByDescending { it.lastModified() } ?: emptyList() }
    var isGoogleSignedIn by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var lastCloudBackup by remember { mutableStateOf("") }
    var showCloudRestoreDialog by remember { mutableStateOf(false) }
    var cloudBackups by remember { mutableStateOf<List<GoogleDriveHelper.CloudBackupInfo>>(emptyList()) }
    var autoBackupFrequency by remember { mutableStateOf("disabled") }
    var autoBackupNextTime by remember { mutableStateOf("") }
    var showAutoBackupDropdown by remember { mutableStateOf(false) }

    val driveHelper = remember { GoogleDriveHelper(context) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isGoogleSignedIn = driveHelper.isSignedIn()
            Toast.makeText(context, "Signed in to Google Drive", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        isGoogleSignedIn = driveHelper.isSignedIn()
        val prefs = context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
        lastBackup = prefs.getString("last_backup", "No backup yet") ?: "No backup yet"
        lastCloudBackup = prefs.getString("last_cloud_backup", "No cloud backup yet") ?: "No cloud backup yet"
        autoBackupFrequency = AutoBackupScheduler.getCurrentFrequency(context)
        autoBackupNextTime = AutoBackupScheduler.getNextBackupTime(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Last backup: $lastBackup", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Text("Save a copy of your database to local storage", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            try {
                                val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.db")
                                dbFile.copyTo(backupFile, overwrite = true)
                                val now = dateFormat.format(Date())
                                context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
                                    .edit().putString("last_backup", now).apply()
                                lastBackup = now
                                backupListVersion++
                                Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                    ) {
                        Text("Backup Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Text("Restore database from a backup file", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (backups.isEmpty()) {
                        Text("No backups available", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        Button(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Restore from Latest Backup", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (backups.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Available Backups (${backups.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        backups.take(5).forEach { backup ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(backup.name, fontSize = 13.sp, color = TextPrimary)
                                    Text(dateFormat.format(Date(backup.lastModified())), fontSize = 11.sp, color = TextSecondary)
                                }
                                IconButton(onClick = {
                                    backup.delete()
                                    backupListVersion++
                                    Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text(autoBackupNextTime, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = showAutoBackupDropdown,
                        onExpandedChange = { showAutoBackupDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = autoBackupFrequency.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Backup Frequency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAutoBackupDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showAutoBackupDropdown,
                            onDismissRequest = { showAutoBackupDropdown = false }
                        ) {
                            listOf("disabled", "daily", "weekly", "monthly").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        autoBackupFrequency = option
                                        showAutoBackupDropdown = false
                                        AutoBackupScheduler.scheduleAutoBackup(context, option)
                                        autoBackupNextTime = AutoBackupScheduler.getNextBackupTime(context)
                                        Toast.makeText(
                                            context,
                                            if (option == "disabled") "Automatic backup disabled" else "Automatic backup set to $option",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Drive Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Sync backups to Google Drive", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isGoogleSignedIn) {
                        Button(
                            onClick = {
                                signInLauncher.launch(driveHelper.getSignInIntent())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Text("Sign in to Google Drive", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Last cloud backup: $lastCloudBackup", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    isBackingUp = true
                                    driveHelper.backup(dbFile) { success, error ->
                                        isBackingUp = false
                                        if (success) {
                                            val now = dateFormat.format(Date())
                                            context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
                                                .edit().putString("last_cloud_backup", now).apply()
                                            lastCloudBackup = now
                                            Toast.makeText(context, "Backed up to Google Drive", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Cloud backup failed: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                                enabled = !isBackingUp
                            ) {
                                Text(if (isBackingUp) "Backing up..." else "Backup to Drive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = {
                                    driveHelper.listBackups { success, list ->
                                        if (success && list != null) {
                                            cloudBackups = list
                                            showCloudRestoreDialog = true
                                        } else {
                                            Toast.makeText(context, "Failed to list backups", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Restore from Drive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                driveHelper.signOut { success ->
                                    if (success) {
                                        isGoogleSignedIn = false
                                        Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("Sign out", color = RedAccent, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Backup", fontWeight = FontWeight.Bold) },
            text = { Text("This will replace your current data with the backup. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val latestBackup = backups.firstOrNull()
                        latestBackup?.copyTo(dbFile, overwrite = true)
                        Toast.makeText(context, "Restore successful! Restart the app.", Toast.LENGTH_LONG).show()
                        showRestoreDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Restore", color = RedAccent) }
            },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCloudRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showCloudRestoreDialog = false },
            title = { Text("Restore from Google Drive", fontWeight = FontWeight.Bold) },
            text = {
                if (cloudBackups.isEmpty()) {
                    Text("No cloud backups found")
                } else {
                    Column {
                        Text("Select a backup to restore:", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        cloudBackups.take(5).forEach { backup ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(backup.fileName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text(backup.createdTime, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    IconButton(onClick = {
                                        try {
                                            driveHelper.restore(backup.fileId, dbFile) { success, error ->
                                                if (success) {
                                                    Toast.makeText(context, "Restore successful! Restart the app.", Toast.LENGTH_LONG).show()
                                                    showCloudRestoreDialog = false
                                                } else {
                                                    Toast.makeText(context, "Restore failed: $error", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(Icons.Filled.CloudDownload, contentDescription = "Restore", tint = Primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCloudRestoreDialog = false }) { Text("Cancel") } }
        )
    }
}
