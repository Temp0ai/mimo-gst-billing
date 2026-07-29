package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.util.Locale
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr3bReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val sales by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val outwardTax = sales.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    val inwardTax = purchases.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    val netTax = outwardTax - inwardTax

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GSTR-3B Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Primary)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("GSTR-3B (Monthly Summary)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }}
            item { ReportDetailRow("Outward Taxable (Sales)", String.format(Locale.US, "\u20B9%,.2f", outwardTax)) }
            item { ReportDetailRow("Inward Taxable (Purchases)", String.format(Locale.US, "\u20B9%,.2f", inwardTax)) }
            item { ReportDetailRow("Net Tax Payable", String.format(Locale.US, "\u20B9%,.2f", netTax), isBold = true) }
            item { ReportDetailRow("Total Sales Invoices", "${sales.size}") }
            item { ReportDetailRow("Total Purchase Invoices", "${purchases.size}") }
        }
    }
}

@Composable
private fun ReportDetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) TextPrimary else TextSecondary)
        Text(value, fontSize = if (isBold) 14.sp else 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = TextPrimary)
    }
}
