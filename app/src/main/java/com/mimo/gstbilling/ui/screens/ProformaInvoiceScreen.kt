package com.mimo.gstbilling.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProformaInvoiceScreen(navController: NavController, invoiceId: Long, viewModel: InvoiceViewModel = hiltViewModel()) {
    var invoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var invoiceItems by remember { mutableStateOf<List<com.mimo.gstbilling.data.local.entity.InvoiceItemEntity>>(emptyList()) }
    var partyName by remember { mutableStateOf("Loading...") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(invoiceId) {
        invoice = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice?.let {
            invoiceItems = viewModel.getItemsForInvoice(it.id)
            partyName = viewModel.getPartyById(it.partyId)?.name ?: "Unknown"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proforma Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = {
                    scope.launch {
                        invoice?.let { inv ->
                            val items = viewModel.getItemsForInvoice(inv.id)
                            val company = viewModel.getCompanyById(1L)
                            val party = viewModel.getPartyById(inv.partyId)
                            val file = PdfGenerator.generateInvoicePdf(context, inv, items, company, party, isThermal = false)
                            PdfGenerator.sharePdf(context, file)
                        }
                    }
                }) { Icon(Icons.Filled.Share, contentDescription = "Share") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("PROFORMA", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Primary)
                                Text(invoice?.invoiceNumber ?: "", fontSize = 12.sp, color = TextSecondary)
                            }
                            Card(shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                                Text("PROFORMA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
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
                                Column(modifier = Modifier.weight(1f)) { Text(item.itemName, fontWeight = FontWeight.Medium, fontSize = 13.sp); Text("${item.quantity} x ${String.format(Locale.US, "\u20B9%,.2f", item.price)}", fontSize = 11.sp, color = TextSecondary) }
                                Text(String.format(Locale.US, "\u20B9%,.2f", item.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", fontWeight = FontWeight.Bold); Text(String.format(Locale.US, "\u20B9%,.2f", invoice?.totalAmount ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                    Text("This is a proforma invoice, not a tax invoice.", fontSize = 12.sp, color = Color(0xFFE65100), modifier = Modifier.padding(12.dp))
                }
            }
            item {
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            navController.navigate(Screen.CreateInvoice.createRoute(partyId = inv.partyId, invoiceType = "sales"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convert to Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
