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
import androidx.compose.material.icons.filled.SwapHoriz
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

data class StockTransferItem(
    val fromWarehouse: String,
    val toWarehouse: String,
    val itemsCount: Int,
    val date: String,
    val status: String,
    val totalValue: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var dateRangeText by remember { mutableStateOf("All Time") }

    val transfers = remember {
        listOf(
            StockTransferItem("Warehouse A", "Warehouse B", 5, "15 Jul 2026", "Completed", 25000.0),
            StockTransferItem("Warehouse A", "Store 1", 3, "12 Jul 2026", "Completed", 12500.0),
            StockTransferItem("Warehouse B", "Store 2", 8, "10 Jul 2026", "Pending", 45000.0),
            StockTransferItem("Store 1", "Warehouse A", 2, "08 Jul 2026", "Completed", 8000.0)
        )
    }

    val totalTransfers = transfers.size
    val totalValue = transfers.sumOf { it.totalValue }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Transfer Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Stock Transfer Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Transfers: $totalTransfers")
                            appendLine("Total Value: ${String.format(Locale.US, "\u20B9%,.2f", totalValue)}")
                            appendLine("---")
                            transfers.forEach { t ->
                                appendLine("${t.fromWarehouse} -> ${t.toWarehouse} | ${t.itemsCount} items | ${String.format(Locale.US, "\u20B9%,.2f", t.totalValue)} | ${t.status}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Stock Transfer Report", text)
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Transfers", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalTransfers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparGreen)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Value", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.0f", totalValue), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            if (transfers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No stock transfers found", color = VyaparTextSecondary)
                    }
                }
            }

            items(transfers) { transfer ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    tint = VyaparBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${transfer.fromWarehouse}  →  ${transfer.toWarehouse}",
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparTextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (transfer.status == "Completed") VyaparSuccessBackground else VyaparWarningBackground
                            ) {
                                Text(
                                    transfer.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = if (transfer.status == "Completed") VyaparGreen else VyaparOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${transfer.itemsCount} items", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(transfer.date, fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", transfer.totalValue),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyaparBlue
                        )
                    }
                }
            }
        }
    }
}
