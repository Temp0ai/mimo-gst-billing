package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMadeScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val unpaidInvoices = invoices.filter { it.totalAmount > it.amountPaid }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Payment Made", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Outstanding Payables", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary); Spacer(modifier = Modifier.height(4.dp)); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", unpaidInvoices.sumOf { it.totalAmount - it.amountPaid }), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = RedAccent) } } }
            items(unpaidInvoices) { invoice ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Balance: ${String.format(java.util.Locale.US, "\u20B9%,.2f", invoice.totalAmount - invoice.amountPaid)}", fontSize = 12.sp, color = RedAccent) }
                    }
                }
            }
            item { if (unpaidInvoices.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("All payments made!", color = GreenBalance, fontWeight = FontWeight.Bold) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
