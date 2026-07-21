package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
    var paymentReminders by remember { mutableStateOf(prefs.getBoolean("payment_reminders", true)) }
    var stockAlerts by remember { mutableStateOf(prefs.getBoolean("stock_alerts", true)) }
    var gstFilingReminder by remember { mutableStateOf(prefs.getBoolean("gst_filing", true)) }
    var dailySummary by remember { mutableStateOf(prefs.getBoolean("daily_summary", false)) }
    var overdueAlerts by remember { mutableStateOf(prefs.getBoolean("overdue_alerts", true)) }
    fun save(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }

    Scaffold(topBar = { TopAppBar(title = { Text("Notification Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    NotifToggleRow("Payment Reminders", "Get reminded about overdue payments", paymentReminders) { paymentReminders = it; save("payment_reminders", it) }
                    NotifToggleRow("Stock Alerts", "Low stock and expiry warnings", stockAlerts) { stockAlerts = it; save("stock_alerts", it) }
                    NotifToggleRow("GST Filing Reminders", "Filing deadline notifications", gstFilingReminder) { gstFilingReminder = it; save("gst_filing", it) }
                    NotifToggleRow("Daily Summary", "Daily business summary at EOD", dailySummary) { dailySummary = it; save("daily_summary", it) }
                    NotifToggleRow("Overdue Alerts", "Alerts for overdue invoices", overdueAlerts) { overdueAlerts = it; save("overdue_alerts", it) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotifToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(title, fontSize = 15.sp, color = TextPrimary); Text(subtitle, fontSize = 11.sp, color = TextSecondary) }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary, uncheckedTrackColor = Color(0xFFBDBDBD)))
        }
        HorizontalDivider(color = Divider)
    }
}
