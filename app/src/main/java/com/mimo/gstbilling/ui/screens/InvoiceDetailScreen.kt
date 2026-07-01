package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    navController: NavController,
    invoiceId: Long = 1L,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    var invoice by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.InvoiceEntity?>(null) }
    var invoiceItems by remember { mutableStateOf<List<com.mimo.gstbilling.data.local.entity.InvoiceItemEntity>>(emptyList()) }

    LaunchedEffect(invoiceId) {
        val inv = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice = inv
        inv?.let {
            invoiceItems = viewModel.getItemsForInvoice(it.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, isThermal = false)
                            PdfGenerator.sharePdf(context, file)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share PDF")
                    }
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, isThermal = true)
                            PdfGenerator.printPdf(context, file)
                        }
                    }) {
                        Icon(Icons.Filled.Print, contentDescription = "Thermal Print")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            viewModel.deleteInvoice(inv)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = RedAccent, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, isThermal = false)
                            PdfGenerator.printPdf(context, file)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueHeader)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Invoice", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        if (invoice == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Loading...", fontSize = 16.sp, color = TextSecondary)
            }
            return@Scaffold
        }

        val inv = invoice!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Invoice No.", fontSize = 12.sp, color = TextSecondary)
                        Text(inv.invoiceNumber, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Date", fontSize = 12.sp, color = TextSecondary)
                        Text(dateFormat.format(Date(inv.invoiceDate)), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text("Party ID: ${inv.partyId}", fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Status: ${inv.paymentStatus.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inv.paymentStatus == "paid") GreenBalance else RedAccent
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenBalance)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Billed Items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Rate excl. tax", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            if (invoiceItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text("No item details available", modifier = Modifier.padding(16.dp), color = TextSecondary)
                    }
                }
            } else {
                itemsIndexed(invoiceItems) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${index + 1}  ${item.itemName}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", item.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Item Subtotal", fontSize = 12.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "%.0f %s x %.2f = \u20B9%,.2f", item.quantity, item.unit, item.price, item.taxableAmount), fontSize = 12.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tax GST@${item.gstRate.toInt()}%", fontSize = 12.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", item.cgstAmount + item.sgstAmount + item.igstAmount), fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 13.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.subTotal), fontSize = 13.sp, color = TextPrimary)
                        }
                        if (inv.discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:", fontSize = 13.sp, color = TextSecondary)
                                Text("-${String.format(Locale.US, "\u20B9%,.2f", inv.discount)}", fontSize = 13.sp, color = RedAccent)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST:", fontSize = 13.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.cgstTotal), fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST:", fontSize = 13.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.sgstTotal), fontSize = 13.sp, color = TextPrimary)
                        }
                        if (inv.igstTotal > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IGST:", fontSize = 13.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.igstTotal), fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                        if (inv.tcsAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TCS (${inv.tcsRate}%):", fontSize = 13.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.tcsAmount), fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                        if (inv.tdsAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TDS (${inv.tdsRate}%):", fontSize = 13.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.tdsAmount), fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BlueHeader)
                        }
                    }
                }
            }

            inv.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notes, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
