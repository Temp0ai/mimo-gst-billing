package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr2ReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val purchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    val totalTaxable = purchases.sumOf { it.taxableAmount }
    val totalCgst = purchases.sumOf { it.cgstTotal }
    val totalSgst = purchases.sumOf { it.sgstTotal }
    val totalIgst = purchases.sumOf { it.igstTotal }
    val totalAmount = purchases.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-2 Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val json = generateGstr2Json(purchases)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, json)
                            putExtra(Intent.EXTRA_SUBJECT, "GSTR-2 Report")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share GSTR-2"))
                    }) { Icon(Icons.Filled.Share, contentDescription = "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GSTR-2: Inward Supplies (Purchases)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Based on ${purchases.size} purchase invoices", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Taxable", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalTaxable)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) }
                            Column { Text("CGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalCgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                            Column { Text("SGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalSgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                            Column { Text("IGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalIgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total ITC Available", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("\u20B9${String.format("%,.0f", totalCgst + totalSgst + totalIgst)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparBlue)
                        }
                    }
                }
            }

            if (purchases.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No purchase invoices found", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(purchases, key = { it.id }) { invoice ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparBlue)
                            }
                            Text("Date: ${dateFormat.format(Date(invoice.invoiceDate))}", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Taxable: \u20B9${String.format("%,.0f", invoice.taxableAmount)}", fontSize = 11.sp, color = TextSecondary)
                                Text("CGST: \u20B9${String.format("%,.0f", invoice.cgstTotal)}", fontSize = 11.sp, color = Primary)
                                Text("SGST: \u20B9${String.format("%,.0f", invoice.sgstTotal)}", fontSize = 11.sp, color = Primary)
                                if (invoice.igstTotal > 0) Text("IGST: \u20B9${String.format("%,.0f", invoice.igstTotal)}", fontSize = 11.sp, color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateGstr2Json(invoices: List<InvoiceEntity>): String {
    val b2b = invoices.map { inv ->
        mapOf(
            "ctin" to (inv.partyId.toString()),
            "inum" to inv.invoiceNumber,
            "idt" to SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(inv.invoiceDate)),
            "val" to inv.totalAmount,
            "pos" to "27",
            "typ" to "R",
            "itms" to listOf(mapOf("num" to 1, "itm_det" to mapOf(
                "rt" to 18.0,
                "txval" to inv.taxableAmount,
                "iamt" to inv.igstTotal,
                "camt" to inv.cgstTotal,
                "samt" to inv.sgstTotal,
                "csamt" to 0.0
            )))
        )
    }

    return org.json.JSONObject().apply {
        put("gstin", "")
        put("fp", SimpleDateFormat("MM-yyyy", Locale.US).format(Date()))
        put("b2b", org.json.JSONArray(b2b.map { org.json.JSONObject(it.toString()) }))
    }.toString(2)
}
