package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillWiseProfitLossReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var dateRangeText by remember { mutableStateOf("All Time") }

    val salesInvoices = invoices.filter { it.invoiceType == "sales" }

    val invoicePnlData = remember(salesInvoices) {
        salesInvoices.map { inv ->
            val cost = inv.totalAmount * 0.6
            val profit = inv.totalAmount - cost
            val margin = if (inv.totalAmount > 0) (profit / inv.totalAmount * 100) else 0.0
            Triple(inv, cost, Pair(profit, margin))
        }
    }

    val totalRevenue = invoicePnlData.sumOf { it.first.totalAmount }
    val totalCost = invoicePnlData.sumOf { it.second }
    val totalProfit = invoicePnlData.sumOf { it.third.first }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Wise P&L Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Bill Wise P&L Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Revenue: ${String.format(Locale.US, "\u20B9%,.2f", totalRevenue)}")
                            appendLine("Total Cost: ${String.format(Locale.US, "\u20B9%,.2f", totalCost)}")
                            appendLine("Total Profit: ${String.format(Locale.US, "\u20B9%,.2f", totalProfit)}")
                            appendLine("---")
                            invoicePnlData.forEach { (inv, cost, pair) ->
                                appendLine("${inv.invoiceNumber} | Revenue: ${String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount)} | Cost: ${String.format(Locale.US, "\u20B9%,.2f", cost)} | Profit: ${String.format(Locale.US, "\u20B9%,.2f", pair.first)} | Margin: ${String.format(Locale.US, "%.1f", pair.second)}%")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Bill Wise P&L Report", text)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Report exported to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VyaparWhite,
                    titleContentColor = VyaparTextPrimary,
                    navigationIconContentColor = VyaparTextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date Range", fontSize = 14.sp, color = VyaparTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dateRangeText, fontSize = 13.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (totalProfit >= 0) VyaparGreen else VyaparRed)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Total Profit/Loss", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", totalProfit),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Revenue", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalRevenue), color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Cost", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalCost), color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Avg Margin", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(
                                "${String.format(Locale.US, "%.1f", if (totalRevenue > 0) (totalProfit / totalRevenue * 100) else 0.0)}%",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            items(invoicePnlData) { (inv, cost, pair) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("Party #${inv.partyId}", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${String.format(Locale.US, "%.1f", pair.second)}% margin",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (pair.first >= 0) VyaparGreen else VyaparRed
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Revenue", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cost", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", cost), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = VyaparDivider)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Profit/Loss", fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", pair.first),
                                fontWeight = FontWeight.Bold,
                                color = if (pair.first >= 0) VyaparGreen else VyaparRed
                            )
                        }
                    }
                }
            }

            if (invoicePnlData.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No invoice data found", color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}
