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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceivedScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val unpaidInvoices = invoices.filter { it.totalAmount > it.amountPaid }
    var showRecordDialog by remember { mutableStateOf(false) }
    var selectedInvoice by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.InvoiceEntity?>(null) }

    if (showRecordDialog && selectedInvoice != null) {
        var amount by remember { mutableStateOf("") }
        var paymentMode by remember { mutableStateOf("Cash") }
        var showModeDropdown by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showRecordDialog = false },
            title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Invoice: ${selectedInvoice!!.invoiceNumber}", fontSize = 14.sp, color = TextSecondary)
                    Text("Outstanding: ${String.format(java.util.Locale.US, "\u20B9%,.2f", selectedInvoice!!.totalAmount - selectedInvoice!!.amountPaid)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Box {
                        OutlinedTextField(value = paymentMode, onValueChange = {}, readOnly = true, label = { Text("Payment Mode") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showModeDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showModeDropdown, onDismissRequest = { showModeDropdown = false }) {
                            listOf("Cash", "UPI", "Bank Transfer", "Cheque", "Credit Card", "Debit Card").forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { paymentMode = m; showModeDropdown = false }) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val paymentAmount = amount.toDoubleOrNull() ?: 0.0
                if (paymentAmount > 0 && selectedInvoice != null) {
                    viewModel.recordPayment(selectedInvoice!!.id, paymentAmount, paymentMode)
                    Toast.makeText(context, "Payment recorded successfully", Toast.LENGTH_SHORT).show()
                    showRecordDialog = false
                }
            }, enabled = (amount.toDoubleOrNull() ?: 0.0) > 0) { Text("Record", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showRecordDialog = false }) { Text("Cancel") } })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Payment Received", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Outstanding Receivables", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary); Spacer(modifier = Modifier.height(4.dp)); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", unpaidInvoices.sumOf { it.totalAmount - it.amountPaid }), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = GreenBalance) } } }
            items(count = unpaidInvoices.size) { index ->
                val invoice = unpaidInvoices[index]
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { selectedInvoice = invoice; showRecordDialog = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Balance: ${String.format(java.util.Locale.US, "\u20B9%,.2f", invoice.totalAmount - invoice.amountPaid)}", fontSize = 12.sp, color = RedAccent) }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
            item { if (unpaidInvoices.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("All payments received!", color = GreenBalance, fontWeight = FontWeight.Bold) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
