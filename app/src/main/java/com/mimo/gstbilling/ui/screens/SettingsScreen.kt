package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            item { Text("Company Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsItem("Manage Companies", Icons.Default.Business, "Add, edit, or switch companies") }
            item { SettingsItem("Business Details", Icons.Default.Business, "Logo, signature, T&Cs") }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Backup & Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsItem("Google Drive Backup", Icons.Default.CloudUpload, "Auto backup to Google Drive") }
            item { SettingsItem("Restore Data", Icons.Default.Backup, "Restore from Google Drive") }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsItem("Notifications", Icons.Default.Notifications, "Manage alerts & reminders") }
            item { SettingsItem("Security", Icons.Default.Security, "App lock & privacy") }
        }
    }
}

@Composable
private fun SettingsItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
