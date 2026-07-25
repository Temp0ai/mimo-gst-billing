package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun TaxRateReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val items by viewModel.getItems().collectAsState(initial = emptyList())

    val taxRates = remember(items) {
        items.groupBy { it.gstRate }.mapValues { (_, groupItems) ->
            val rateInvoices = invoices.filter { inv ->
                groupItems.any { it.name == inv.invoiceNumber || true }
            }
            mapOf(
                "itemsCount" to groupItems.size,
                "taxableAmount" to rateInvoices.sumOf { it.taxableAmount },
                "taxAmount" to rateInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
            )
        }
    }

    val totalItems = taxRates.values.sumOf { (it["itemsCount"] as? Int) ?: 0 }
    val totalTaxable = taxRates.values.sumOf { (it["taxableAmount"] as? Double) ?: 0.0 }
    val totalTax = taxRates.values.sumOf { (it["taxAmount"] as? Double) ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tax Rate Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Tax Rate Report")
                            appendLine("Total Items: $totalItems")
                            appendLine("Total Taxable: ${String.format(Locale.US, "\u20B9%,.2f", totalTaxable)}")
                            appendLine("Total Tax: ${String.format(Locale.US, "\u20B9%,.2f", totalTax)}")
                            appendLine("---")
                            taxRates.forEach { (rate, data) ->
                                appendLine("${rate.toInt()}% | Items: ${data["itemsCount"]} | Taxable: ${String.format(Locale.US, "\u20B9%,.2f", data["taxableAmount"])} | Tax: ${String.format(Locale.US, "\u20B9%,.2f", data["taxAmount"])}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Tax Rate Report", text)
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
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Tax Rate Summary", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("$totalItems", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Taxable", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalTaxable), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Tax", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalTax), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (taxRates.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No tax rate data found", color = VyaparTextSecondary)
                    }
                }
            }

            items(taxRates.entries.sortedByDescending { it.key }) { (rate, data) ->
                val itemsCount = (data["itemsCount"] as? Int) ?: 0
                val taxable = (data["taxableAmount"] as? Double) ?: 0.0
                val tax = (data["taxAmount"] as? Double) ?: 0.0

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
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = VyaparBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "${rate.toInt()}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = VyaparBlue
                                )
                            }
                            Text("$itemsCount items", fontSize = 13.sp, color = VyaparTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable Amount", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", taxable), fontSize = 13.sp, color = VyaparTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax Amount", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", tax), fontSize = 13.sp, color = VyaparRed, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${taxRates.size} rates", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalTax), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparBlue)
                        }
                    }
                }
            }
        }
    }
}
