package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSyncSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE) }
    var autoSync by remember { mutableStateOf(prefs.getBoolean("auto_sync_enabled", true)) }
    var syncInterval by remember { mutableIntStateOf(prefs.getInt("auto_sync_interval", 15)) }
    var syncOnWifiOnly by remember { mutableStateOf(prefs.getBoolean("auto_sync_wifi_only", false)) }
    var syncInvoices by remember { mutableStateOf(prefs.getBoolean("auto_sync_invoices", true)) }
    var syncParties by remember { mutableStateOf(prefs.getBoolean("auto_sync_parties", true)) }
    var syncItems by remember { mutableStateOf(prefs.getBoolean("auto_sync_items", true)) }
    var syncExpenses by remember { mutableStateOf(prefs.getBoolean("auto_sync_expenses", true)) }
    var lastSyncTime by remember { mutableStateOf(prefs.getString("auto_sync_last_time", "Never") ?: "Never") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Auto-Sync Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Auto-Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Keep data synced across devices", fontSize = 12.sp, color = VyaparTextSecondary) }
                        Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                    }
                    if (autoSync) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sync Interval", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 15, 30, 60).forEach { minutes ->
                                FilterChip(selected = syncInterval == minutes, onClick = { syncInterval = minutes }, label = { Text("${minutes}m") })
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wi-Fi only", fontSize = 14.sp); Switch(checked = syncOnWifiOnly, onCheckedChange = { syncOnWifiOnly = it })
                        }
                    }
                }
            }
            if (autoSync) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sync Data", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Invoices", fontSize = 14.sp); Switch(checked = syncInvoices, onCheckedChange = { syncInvoices = it }) }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Parties", fontSize = 14.sp); Switch(checked = syncParties, onCheckedChange = { syncParties = it }) }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Items", fontSize = 14.sp); Switch(checked = syncItems, onCheckedChange = { syncItems = it }) }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Expenses", fontSize = 14.sp); Switch(checked = syncExpenses, onCheckedChange = { syncExpenses = it }) }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column { Text("Last synced: $lastSyncTime", fontSize = 13.sp, color = VyaparTextSecondary); Text("Next sync in ${syncInterval} minutes", fontSize = 12.sp, color = VyaparBlue) }
                    }
                }
            }
            Button(onClick = {
                prefs.edit().apply {
                    putBoolean("auto_sync_enabled", autoSync)
                    putInt("auto_sync_interval", syncInterval)
                    putBoolean("auto_sync_wifi_only", syncOnWifiOnly)
                    putBoolean("auto_sync_invoices", syncInvoices)
                    putBoolean("auto_sync_parties", syncParties)
                    putBoolean("auto_sync_items", syncItems)
                    putBoolean("auto_sync_expenses", syncExpenses)
                }.apply()
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                Text("Save Settings", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
