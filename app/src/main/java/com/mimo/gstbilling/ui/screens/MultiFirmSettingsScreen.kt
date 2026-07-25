package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFirmSettingsScreen(
    navController: NavController
) {
    var multiFirmEnabled by remember { mutableStateOf(true) }
    var sharedInvoices by remember { mutableStateOf(true) }
    var sharedItems by remember { mutableStateOf(false) }
    var sharedParties by remember { mutableStateOf(false) }

    data class Firm(val name: String, val gstin: String, val isPrimary: Boolean, val isAdmin: Boolean)

    val firms = remember {
        mutableStateListOf(
            Firm("Mimo Technologies Pvt Ltd", "27AABCM1234R1ZM", true, true),
            Firm("Mimo Traders", "27AABCM5678R1ZM", false, false),
            Firm("Mimo Services", "27AABCM9012R1ZM", false, false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Firm Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-Firm Mode", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Text("Manage multiple businesses in one app", fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = multiFirmEnabled, onCheckedChange = { multiFirmEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary, checkedThumbColor = Color.White)
                    )
                }
            }

            if (multiFirmEnabled) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Firms (${firms.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        firms.forEach { firm ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(
                                    if (firm.isPrimary) Primary else TextSecondary.copy(alpha = 0.3f)
                                ), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(firm.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                    Text(firm.gstin, fontSize = 12.sp, color = TextSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (firm.isPrimary) {
                                            Text("Primary", fontSize = 10.sp, color = Color.White,
                                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Primary).padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        if (firm.isAdmin) {
                                            Text("Admin", fontSize = 10.sp, color = Color.White,
                                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF1B5E20)).padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                if (!firm.isPrimary) {
                                    TextButton(onClick = { /* switch firm */ }) {
                                        Text("Switch", fontSize = 13.sp, color = Primary)
                                    }
                                }
                            }
                            if (firms.indexOf(firm) < firms.lastIndex) HorizontalDivider(color = Divider)
                        }
                    }
                }

                Button(
                    onClick = { /* add firm */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null); Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Firm", fontWeight = FontWeight.SemiBold)
                }

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Shared Settings", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SharedSettingRow(title = "Share Invoices", subtitle = "Same invoice templates across firms", checked = sharedInvoices, onCheckedChange = { sharedInvoices = it })
                        HorizontalDivider(color = Divider)
                        SharedSettingRow(title = "Share Items", subtitle = "Common item catalog for all firms", checked = sharedItems, onCheckedChange = { sharedItems = it })
                        HorizontalDivider(color = Divider)
                        SharedSettingRow(title = "Share Parties", subtitle = "Shared party/customer list", checked = sharedParties, onCheckedChange = { sharedParties = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SharedSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary, checkedThumbColor = Color.White)
        )
    }
}
