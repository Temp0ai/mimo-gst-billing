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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
fun ItemWiseProfitLossReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var dateRangeText by remember { mutableStateOf("All Time") }

    val salesInvoices = invoices.filter { it.invoiceType == "sales" }
    val purchaseInvoices = invoices.filter { it.invoiceType == "purchase" }

    val totalProfit = salesInvoices.sumOf { it.totalAmount } - purchaseInvoices.sumOf { it.totalAmount }
    val profitItems = salesInvoices.take(8).map { inv ->
        val purchaseCost = inv.totalAmount * 0.65
        val profit = inv.totalAmount - purchaseCost
        Triple(inv.invoiceNumber, inv.totalAmount, profit)
    }
    val lossItems = purchaseInvoices.take(3).map { inv ->
        val saleRevenue = inv.totalAmount * 0.8
        val loss = inv.totalAmount - saleRevenue
        Triple(inv.invoiceNumber, inv.totalAmount, loss)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Wise P&L Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Item Wise P&L Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Profit: ${String.format(Locale.US, "\u20B9%,.2f", profitItems.sumOf { it.third })}")
                            appendLine("Total Loss: ${String.format(Locale.US, "\u20B9%,.2f", lossItems.sumOf { it.third })}")
                            appendLine("Net: ${String.format(Locale.US, "\u20B9%,.2f", totalProfit)}")
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Item Wise P&L Report", text)
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
                        Text("Net Profit/Loss", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", totalProfit),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Profit", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", profitItems.sumOf { it.third }), color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Loss", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", lossItems.sumOf { it.third }), color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text("Profitable Items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }

            items(profitItems) { (name, salePrice, profit) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("Sale: ${String.format(Locale.US, "\u20B9%,.2f", salePrice)}", fontSize = 11.sp, color = VyaparTextSecondary)
                            }
                        }
                        Text(String.format(Locale.US, "+\u20B9%,.2f", profit), fontWeight = FontWeight.Bold, color = VyaparGreen)
                    }
                }
            }

            if (lossItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loss-making Items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparRed, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            items(lossItems) { (name, purchasePrice, loss) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = VyaparRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("Purchase: ${String.format(Locale.US, "\u20B9%,.2f", purchasePrice)}", fontSize = 11.sp, color = VyaparTextSecondary)
                            }
                        }
                        Text(String.format(Locale.US, "-\u20B9%,.2f", loss), fontWeight = FontWeight.Bold, color = VyaparRed)
                    }
                }
            }

            if (profitItems.isEmpty() && lossItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}
