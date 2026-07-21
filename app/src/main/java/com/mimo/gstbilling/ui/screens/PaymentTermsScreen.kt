package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PaymentTermsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("payment_terms", Context.MODE_PRIVATE)

    var advancePayment by remember { mutableStateOf(prefs.getBoolean("advance_payment", false)) }
    var dueDateEnabled by remember { mutableStateOf(prefs.getBoolean("due_date_enabled", true)) }
    var defaultDueDays by remember { mutableStateOf(prefs.getInt("default_due_days", 30)) }
    var paymentReminderDays by remember { mutableStateOf(prefs.getInt("payment_reminder_days", 7)) }

    fun save(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
            }
            apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Terms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column {
                        SettingToggleRow("Require Advance Payment", advancePayment) { advancePayment = it; save("advance_payment", it) }
                        SettingToggleRow("Enable Due Dates", dueDateEnabled) { dueDateEnabled = it; save("due_date_enabled", it) }

                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Default Due Days", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (defaultDueDays > 1) { defaultDueDays--; save("default_due_days", defaultDueDays) } }) {
                                    Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary)
                                }
                                Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(defaultDueDays.toString(), fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                                IconButton(onClick = { defaultDueDays++; save("default_due_days", defaultDueDays) }) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Primary)
                                }
                            }
                        }
                        HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Payment Reminder (days before)", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (paymentReminderDays > 1) { paymentReminderDays--; save("payment_reminder_days", paymentReminderDays) } }) {
                                    Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary)
                                }
                                Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(paymentReminderDays.toString(), fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                                IconButton(onClick = { paymentReminderDays++; save("payment_reminder_days", paymentReminderDays) }) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
