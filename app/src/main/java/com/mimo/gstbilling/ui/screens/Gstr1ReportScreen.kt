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
import androidx.compose.ui.Alignment
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
fun Gstr1ReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val totalTaxable = invoices.sumOf { it.taxableAmount }
    val totalCgst = invoices.sumOf { it.cgstTotal }
    val totalSgst = invoices.sumOf { it.sgstTotal }
    val totalIgst = invoices.sumOf { it.igstTotal }
    val totalInvoiceVal = invoices.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GSTR-1 Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("GSTR-1 (Outward Supplies)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Invoices: ${invoices.size}", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }}
            item { ReportDetailRow("Total Taxable Value", String.format(Locale.US, "\u20B9%,.2f", totalTaxable)) }
            item { ReportDetailRow("CGST", String.format(Locale.US, "\u20B9%,.2f", totalCgst)) }
            item { ReportDetailRow("SGST", String.format(Locale.US, "\u20B9%,.2f", totalSgst)) }
            item { ReportDetailRow("IGST", String.format(Locale.US, "\u20B9%,.2f", totalIgst)) }
            item { ReportDetailRow("Total Invoice Value", String.format(Locale.US, "\u20B9%,.2f", totalInvoiceVal), isBold = true) }
        }
    }
}

@Composable
fun ReportDetailRow(label: String, value: String, isBold: Boolean = false) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, color = TextPrimary)
            Text(value, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = if (isBold) Primary else TextPrimary)
        }
    }
}
