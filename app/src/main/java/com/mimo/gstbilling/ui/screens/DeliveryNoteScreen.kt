package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteScreen(navController: NavController, invoiceId: Long, viewModel: InvoiceViewModel = hiltViewModel()) {
    var invoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var invoiceItems by remember { mutableStateOf<List<com.mimo.gstbilling.data.local.entity.InvoiceItemEntity>>(emptyList()) }
    var partyName by remember { mutableStateOf("Loading...") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    LaunchedEffect(invoiceId) {
        invoice = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice?.let {
            invoiceItems = viewModel.getItemsForInvoice(it.id)
            partyName = viewModel.getPartyById(it.partyId)?.name ?: "Unknown"
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Delivery Note", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("DELIVERY NOTE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Primary); Text(invoice?.invoiceNumber ?: "", fontSize = 12.sp, color = TextSecondary) }
                            Card(shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) { Text("DELIVERY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Party: $partyName", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text("Date: ${invoice?.let { dateFormat.format(Date(it.invoiceDate)) } ?: ""}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                        invoiceItems.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) { Text(item.itemName, fontWeight = FontWeight.Medium, fontSize = 13.sp); Text("Qty: ${item.quantity} ${item.unit}", fontSize = 11.sp, color = TextSecondary) }
                            }
                        }
                    }
                }
            }
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) { Text("This is a delivery note. No payment is due against this document.", fontSize = 12.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp)) } }
        }
    }
}
