package com.mimo.gstbilling.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
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
import com.mimo.gstbilling.ui.theme.*
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
    var showBackupSuccess by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    val backupDir = remember { File(context.filesDir, "backups").apply { mkdirs() } }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
        lastBackup = prefs.getString("last_backup", "No backup yet") ?: "No backup yet"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
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

            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
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
                                showBackupSuccess = true
                                Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                    ) {
                        Text("Backup Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Text("Restore database from a backup file", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    val backups = backupDir.listFiles()?.filter { it.extension == "db" }?.sortedByDescending { it.lastModified() } ?: emptyList()
                    if (backups.isEmpty()) {
                        Text("No backups available", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        Button(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Restore from Latest Backup", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (backups.isNotEmpty()) {
                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
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
                                    Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                }
                            }
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
                        val latestBackup = backupDir.listFiles()?.filter { it.extension == "db" }?.maxByOrNull { it.lastModified() }
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
}
