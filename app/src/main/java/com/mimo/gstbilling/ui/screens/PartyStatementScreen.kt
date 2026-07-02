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
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyStatementScreen(navController: NavController, partyId: Long = 1L, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val partyInvoices = invoices.filter { it.partyId == partyId }
    val totalAmount = partyInvoices.sumOf { it.totalAmount }
    val totalPaid = partyInvoices.sumOf { it.amountPaid }
    val balance = totalAmount - totalPaid

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Party Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = BlueHeader)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Party ID: $partyId", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalAmount), color = Color.White, fontWeight = FontWeight.Bold) }
                            Column(horizontalAlignment = Alignment.End) { Text("Paid", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalPaid), color = Color.White, fontWeight = FontWeight.Bold) }
                            Column(horizontalAlignment = Alignment.End) { Text("Balance", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", balance), color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
            if (partyInvoices.isEmpty()) {
                item { Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("No transactions for this party", fontSize = 14.sp, color = TextSecondary) } }
            } else {
                items(partyInvoices) { inv ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(dateFormat.format(Date(inv.invoiceDate)), fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontWeight = FontWeight.Bold, color = Primary)
                                Text(inv.paymentStatus.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (inv.paymentStatus == "paid") GreenBalance else RedAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
