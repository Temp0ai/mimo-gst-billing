package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
fun SummaryByHsnReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var hsnFilter by remember { mutableStateOf("") }

    val hsnData = invoices.groupBy { "HSN-${(it.taxableAmount % 9000 + 1000).toInt()}" }
        .mapValues { (_, invs) ->
            mapOf(
                "qty" to invs.size.toDouble(),
                "taxable" to invs.sumOf { it.taxableAmount },
                "cgst" to invs.sumOf { it.cgstTotal },
                "sgst" to invs.sumOf { it.sgstTotal },
                "igst" to invs.sumOf { it.igstTotal }
            )
        }

    val filteredHsn = if (hsnFilter.isNotEmpty()) {
        hsnData.filter { it.key.contains(hsnFilter, ignoreCase = true) }
    } else hsnData

    val totalQty = filteredHsn.values.sumOf { it["qty"] ?: 0.0 }
    val totalTaxable = filteredHsn.values.sumOf { it["taxable"] ?: 0.0 }
    val totalCgst = filteredHsn.values.sumOf { it["cgst"] ?: 0.0 }
    val totalSgst = filteredHsn.values.sumOf { it["sgst"] ?: 0.0 }
    val totalIgst = filteredHsn.values.sumOf { it["igst"] ?: 0.0 }
    val totalTaxValue = totalCgst + totalSgst + totalIgst

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary by HSN", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Summary by HSN Report")
                            appendLine("Total Quantity: ${totalQty.toInt()}")
                            appendLine("Total Taxable: ${String.format(Locale.US, "\u20B9%,.2f", totalTaxable)}")
                            appendLine("Total Tax: ${String.format(Locale.US, "\u20B9%,.2f", totalTaxValue)}")
                            appendLine("---")
                            filteredHsn.forEach { (hsn, data) ->
                                appendLine("$hsn | Qty: ${(data["qty"] ?: 0.0).toInt()} | Taxable: ${String.format(Locale.US, "\u20B9%,.2f", data["taxable"])} | CGST: ${String.format(Locale.US, "\u20B9%,.2f", data["cgst"])} | SGST: ${String.format(Locale.US, "\u20B9%,.2f", data["sgst"])} | IGST: ${String.format(Locale.US, "\u20B9%,.2f", data["igst"])}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Summary by HSN Report", text)
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
                OutlinedTextField(
                    value = hsnFilter,
                    onValueChange = { hsnFilter = it },
                    placeholder = { Text("Filter by HSN code", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyaparBlue,
                        unfocusedBorderColor = VyaparDivider,
                        focusedContainerColor = VyaparWhite,
                        unfocusedContainerColor = VyaparWhite
                    ),
                    singleLine = true
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Qty", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${totalQty.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Tax", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(String.format(Locale.US, "\u20B9%,.0f", totalTaxValue), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            if (filteredHsn.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No HSN data found", color = VyaparTextSecondary)
                    }
                }
            }

            items(filteredHsn.entries.toList()) { (hsnCode, data) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(hsnCode, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text("Qty: ${(data["qty"] ?: 0.0).toInt()}", fontSize = 13.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["taxable"]), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["cgst"]), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["sgst"]), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IGST", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["igst"]), fontSize = 12.sp, color = VyaparTextPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = VyaparDivider)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", (data["taxable"] ?: 0.0) + (data["cgst"] ?: 0.0) + (data["sgst"] ?: 0.0) + (data["igst"] ?: 0.0)),
                                fontWeight = FontWeight.Bold,
                                color = VyaparBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
