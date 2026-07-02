package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
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
fun PaymentRemindersScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val pendingInvoices = invoices.filter { it.paymentStatus != "paid" }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Payment Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        if (pendingInvoices.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No pending payments", fontSize = 16.sp, color = TextSecondary)
                Text("All invoices are paid!", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = RedAccent)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pending Invoices", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${pendingInvoices.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                items(pendingInvoices) { inv ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Party ID: ${inv.partyId}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount - inv.amountPaid), fontWeight = FontWeight.Bold, color = RedAccent)
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send Reminder", tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
