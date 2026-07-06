package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(navController: NavController, title: String) {
    var toggle1 by remember { mutableStateOf(true) }
    var toggle2 by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }

    val settingsConfig = when (title) {
        "Tax Configuration" -> listOf(
            "Enable GST" to true,
            "Enable IGST" to false,
            "Auto-calculate GST" to true,
            "Show HSN on Invoice" to true,
            "Show GST Breakup" to true
        )
        "Transaction Settings" -> listOf(
            "Auto-generate Invoice Number" to true,
            "Show Profit in Invoice" to false,
            "Enable TCS/TDS" to false,
            "Default Payment Status" to true,
            "Enable Partial Payment" to true
        )
        "Payment Settings" -> listOf(
            "Enable UPI Payment" to true,
            "Enable Cash Payment" to true,
            "Enable Cheque Payment" to false,
            "Enable Card Payment" to false,
            "Auto-reconcile Payments" to false
        )
        "Print Format" -> listOf(
            "Show Company Logo" to true,
            "Show GSTIN" to true,
            "Show HSN Code" to true,
            "Show Bank Details" to false,
            "Show Terms & Conditions" to true,
            "Show Signature Line" to false
        )
        "Print Size" -> listOf(
            "A4 Size (Default)" to true,
            "Thermal 58mm" to false,
            "Thermal 80mm" to false
        )
        "Invoice Settings" -> listOf(
            "Enable Invoice Discount" to true,
            "Enable Item Discount" to true,
            "Show Due Date" to false,
            "Enable Round Off" to true,
            "Show Amount in Words" to true
        )
        "SMS Templates" -> listOf(
            "Send Invoice SMS" to false,
            "Send Payment Reminder SMS" to false,
            "Send Delivery SMS" to false
        )
        "Auto Send" -> listOf(
            "Auto-send Invoice on Save" to false,
            "Auto-send Payment Reminder" to false,
            "Auto-send Receipt" to false
        )
        "Stock Alerts" -> listOf(
            "Enable Low Stock Alert" to true,
            "Show Stock on Invoice" to true,
            "Deduct Stock on Sale" to true
        )
        "Add Staff" -> listOf(
            "Enable Staff Login" to false,
            "Allow Staff to Create Invoice" to true,
            "Allow Staff to Edit Invoice" to false,
            "Allow Staff to Delete Invoice" to false
        )
        "Manage Permissions" -> listOf(
            "View Sales" to true,
            "Create Sales" to true,
            "Edit Sales" to false,
            "Delete Sales" to false,
            "View Reports" to true,
            "View Settings" to false
        )
        "Units & Categories" -> listOf(
            "Enable Multiple Units" to false,
            "Enable Item Categories" to true,
            "Enable Item Variants" to false
        )
        "Currency Settings" -> listOf(
            "Use Indian Rupee (₹)" to true,
            "Show Currency Symbol" to true,
            "Decimal Places: 2" to true
        )
        "TCS/TDS Settings" -> listOf(
            "Enable TCS" to false,
            "Enable TDS" to false,
            "TCS Rate %" to false,
            "TDS Rate %" to false
        )
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            if (settingsConfig.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        settingsConfig.forEach { (label, defaultValue) ->
                            var checked by remember { mutableStateOf(defaultValue) }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                Switch(checked = checked, onCheckedChange = { checked = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                            }
                            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Settings will be available soon", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
