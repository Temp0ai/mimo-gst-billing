package com.mimo.gstbilling.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr3bReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val sales by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val context = LocalContext.current

    val outwardTaxable = sales.sumOf { it.taxableAmount }
    val inwardTaxable = purchases.sumOf { it.taxableAmount }
    val outwardCgst = sales.sumOf { it.cgstTotal }
    val outwardSgst = sales.sumOf { it.sgstTotal }
    val outwardIgst = sales.sumOf { it.igstTotal }
    val inwardCgst = purchases.sumOf { it.cgstTotal }
    val inwardSgst = purchases.sumOf { it.sgstTotal }
    val inwardIgst = purchases.sumOf { it.igstTotal }
    val netCgst = outwardCgst - inwardCgst
    val netSgst = outwardSgst - inwardSgst
    val netIgst = outwardIgst - inwardIgst
    val netTax = netCgst + netSgst + netIgst

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GSTR-3B Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val json = generateGstr3bJson(outwardTaxable, inwardTaxable, outwardCgst, outwardSgst, outwardIgst, inwardCgst, inwardSgst, inwardIgst)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, json)
                            putExtra(Intent.EXTRA_SUBJECT, "GSTR-3B Report")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share GSTR-3B"))
                    }) { Icon(Icons.Filled.Share, contentDescription = "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Primary)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("GSTR-3B (Monthly Summary)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Period: ${SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }}

            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3.1 Outward Taxable Supplies", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    ReportDetailRow("Total Taxable Value", String.format(Locale.US, "\u20B9%,.2f", outwardTaxable))
                    ReportDetailRow("Integrated Tax (IGST)", String.format(Locale.US, "\u20B9%,.2f", outwardIgst))
                    ReportDetailRow("Central Tax (CGST)", String.format(Locale.US, "\u20B9%,.2f", outwardCgst))
                    ReportDetailRow("State Tax (SGST)", String.format(Locale.US, "\u20B9%,.2f", outwardSgst))
                    Text("Total Outward Tax", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparBlue)
                }
            }}

            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3.2 Inward Taxable Supplies (ITC)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    ReportDetailRow("Total Taxable Value", String.format(Locale.US, "\u20B9%,.2f", inwardTaxable))
                    ReportDetailRow("Integrated Tax (IGST)", String.format(Locale.US, "\u20B9%,.2f", inwardIgst))
                    ReportDetailRow("Central Tax (CGST)", String.format(Locale.US, "\u20B9%,.2f", inwardCgst))
                    ReportDetailRow("State Tax (SGST)", String.format(Locale.US, "\u20B9%,.2f", inwardSgst))
                    Text("Total ITC Available", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparGreen)
                }
            }}

            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = VyaparBlue), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("5.1 Net Tax Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    ReportDetailRow("Integrated Tax", String.format(Locale.US, "\u20B9%,.2f", netIgst), Color.White)
                    ReportDetailRow("Central Tax", String.format(Locale.US, "\u20B9%,.2f", netCgst), Color.White)
                    ReportDetailRow("State Tax", String.format(Locale.US, "\u20B9%,.2f", netSgst), Color.White)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Net Tax", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(String.format(Locale.US, "\u20B9%,.2f", netTax), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                }
            }}

            item { ReportDetailRow("Total Sales Invoices", "${sales.size}") }
            item { ReportDetailRow("Total Purchase Invoices", "${purchases.size}") }
        }
    }
}

@Composable
private fun ReportDetailRow(label: String, value: String, valueColor: Color = TextPrimary, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) TextPrimary else TextSecondary)
        Text(value, fontSize = if (isBold) 14.sp else 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = valueColor)
    }
}

private fun generateGstr3bJson(outwardTaxable: Double, inwardTaxable: Double, outwardCgst: Double, outwardSgst: Double, outwardIgst: Double, inwardCgst: Double, inwardSgst: Double, inwardIgst: Double): String {
    return org.json.JSONObject().apply {
        put("gstin", "")
        put("fp", SimpleDateFormat("MM-yyyy", Locale.US).format(Date()))
        put("b2b_out", org.json.JSONObject().apply {
            put("txval", outwardTaxable)
            put("iamt", outwardIgst)
            put("camt", outwardCgst)
            put("samt", outwardSgst)
        })
        put("b2b_in", org.json.JSONObject().apply {
            put("txval", inwardTaxable)
            put("iamt", inwardIgst)
            put("camt", inwardCgst)
            put("samt", inwardSgst)
        })
        put("itc", org.json.JSONObject().apply {
            put("iamt", inwardIgst)
            put("camt", inwardCgst)
            put("samt", inwardSgst)
        })
        put("tax_payable", org.json.JSONObject().apply {
            put("iamt", outwardIgst - inwardIgst)
            put("camt", outwardCgst - inwardCgst)
            put("samt", outwardSgst - inwardSgst)
        })
    }.toString(2)
}
