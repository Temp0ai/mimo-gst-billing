package com.mimo.gstbilling.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitOnInvoiceScreen(
    navController: NavController
) {
    val invoiceNumber = "INV-0006"
    val totalProfit = 8400.0
    val totalRevenue = 25960.0

    data class ItemProfit(val name: String, val costPrice: Double, val salePrice: Double, val margin: Double, val profit: Double)

    val itemProfits = listOf(
        ItemProfit("Laptop HP 15s", 38000.0, 45000.0, 18.4, 7000.0),
        ItemProfit("Mouse Logitech", 320.0, 450.0, 40.6, 1300.0),
        ItemProfit("LED Bulb 9W", 85.0, 120.0, 41.2, 175.0),
        ItemProfit("Consulting Service", 0.0, 5000.0, 100.0, -750.0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit on Invoice", fontWeight = FontWeight.Bold) },
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Invoice: $invoiceNumber", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Revenue", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${String.format("%.0f", totalRevenue)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Profit", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${String.format("%.0f", totalProfit)}", fontWeight = FontWeight.Bold, color = GreenBalance, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val profitMargin = (totalProfit / totalRevenue * 100)
                    LinearProgressIndicator(
                        progress = { (profitMargin / 100).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = GreenBalance,
                        trackColor = GreenBalance.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Profit Margin: ${String.format("%.1f", profitMargin)}%", fontSize = 13.sp, color = GreenBalance, fontWeight = FontWeight.SemiBold)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Item-wise Profit Breakdown", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    itemProfits.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text("Cost: ₹${String.format("%.0f", item.costPrice)} | Sale: ₹${String.format("%.0f", item.salePrice)}", fontSize = 12.sp, color = TextSecondary)
                                Text("Margin: ${String.format("%.1f", item.margin)}%", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(
                                "${if (item.profit >= 0) "+" else ""}₹${String.format("%.0f", item.profit)}",
                                fontWeight = FontWeight.Bold,
                                color = if (item.profit >= 0) GreenBalance else RedAccent,
                                fontSize = 14.sp
                            )
                        }
                        HorizontalDivider(color = Divider)
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notes", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "Consulting service shows loss due to included travel expenses.", onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider),
                        minLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
