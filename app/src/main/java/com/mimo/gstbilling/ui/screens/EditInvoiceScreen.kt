package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    navController: NavController,
    invoiceId: Long = 0L,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var invoice by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.InvoiceEntity?>(null) }
    var amountPaid by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        invoice = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice?.let {
            amountPaid = String.format(java.util.Locale.US, "%.2f", it.amountPaid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (invoice == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val inv = invoice!!
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Invoice Details", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice No:", color = TextSecondary); Text(inv.invoiceNumber, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status:", color = TextSecondary); Text(inv.paymentStatus.uppercase(), fontWeight = FontWeight.Bold,
                                color = when(inv.paymentStatus) { "paid" -> GreenBalance; "partial" -> Color(0xFFFF9800); else -> RedAccent })
                        }
                    }
                }

                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Payment Details", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount:", color = TextSecondary); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", inv.totalAmount), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Already Paid:", color = TextSecondary); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", inv.amountPaid), color = GreenBalance)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Due:", color = TextSecondary); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", inv.totalAmount - inv.amountPaid), fontWeight = FontWeight.Bold, color = RedAccent)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountPaid,
                            onValueChange = { amountPaid = it },
                            label = { Text("Amount Paid") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            prefix = { Text("\u20B9 ") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = false, onClick = { amountPaid = "0" }, label = { Text("Unpaid") })
                            FilterChip(selected = false, onClick = { amountPaid = String.format(java.util.Locale.US, "%.2f", inv.totalAmount / 2) }, label = { Text("50%") })
                            FilterChip(selected = false, onClick = { amountPaid = String.format(java.util.Locale.US, "%.2f", inv.totalAmount) }, label = { Text("Full") })
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val paid = amountPaid.toDoubleOrNull() ?: 0.0
                        viewModel.updatePaymentStatus(invoiceId, paid)
                        scope.launch { navController.popBackStack() }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
