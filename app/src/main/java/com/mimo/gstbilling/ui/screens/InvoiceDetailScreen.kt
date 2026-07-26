package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

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
    var party by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.PartyEntity?>(null) }
    var partyName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("My Business") }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        val inv = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice = inv
        inv?.let {
            invoiceItems = viewModel.getItemsForInvoice(it.id)
            val p = viewModel.getPartyById(it.partyId)
            party = p
            partyName = p?.name ?: "Unknown Party"
            val company = viewModel.getCompanyById(it.companyId)
            companyName = company?.name ?: "My Business"
        }
    }

    if (showRecordPaymentDialog && invoice != null) {
        val inv = invoice!!
        val remaining = inv.totalAmount - inv.amountPaid
        var amount by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", remaining)) }
        var paymentMode by remember { mutableStateOf("Cash") }
        var showModeDropdown by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Invoice: ${inv.invoiceNumber}", fontSize = 14.sp, color = TextSecondary)
                    Text("Outstanding: ${String.format(java.util.Locale.US, "\u20B9%,.2f", remaining)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent)
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
                if (paymentAmount > 0) {
                    viewModel.recordPayment(inv.id, paymentAmount, paymentMode)
                    Toast.makeText(context, "Payment recorded successfully", Toast.LENGTH_SHORT).show()
                    showRecordPaymentDialog = false
                    scope.launch {
                        invoice = viewModel.getInvoiceByIdDirect(invoiceId)
                    }
                }
            }, enabled = (amount.toDoubleOrNull() ?: 0.0) > 0) { Text("Record", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showRecordPaymentDialog = false }) { Text("Cancel") } })
    }

    if (showDeleteDialog && invoice != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Invoice", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this invoice? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    invoice?.let { viewModel.deleteInvoice(it) }
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
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
                            navController.navigate(Screen.InvoicePreview.createRoute(inv.id))
                        }
                    }) {
                        Icon(Icons.Filled.Print, contentDescription = "Preview & Print", tint = VyaparBlue)
                    }
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            val format = PdfGenerator.getPrintFormat(context)
                            val isThermal = PdfGenerator.isThermal(format)
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, party, isThermal = isThermal)
                            PdfGenerator.sharePdf(context, file)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share PDF", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            navController.navigate(Screen.EditInvoice.createRoute(inv.id))
                        }
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Invoice")
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Convert to Delivery Challan") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "delivery_challan"))
                                },
                                leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, tint = Primary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Create Credit Note") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "credit_note"))
                                },
                                leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, tint = RedAccent) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete Invoice", color = RedAccent) },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextSecondary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (invoice != null && invoice!!.totalAmount > invoice!!.amountPaid) {
                    Button(
                        onClick = { showRecordPaymentDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                    ) {
                        Icon(Icons.Filled.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val format = PdfGenerator.getPrintFormat(context)
                            val isThermal = PdfGenerator.isThermal(format)
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, party, isThermal = isThermal)
                            PdfGenerator.sharePdfToWhatsApp(context, file)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val format = PdfGenerator.getPrintFormat(context)
                            val isThermal = PdfGenerator.isThermal(format)
                            val file = PdfGenerator.generateInvoicePdf(context, inv, invoiceItems, null, party, isThermal = isThermal)
                            PdfGenerator.printPdf(context, file)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val message = buildString {
                                append("Hi ${partyName},\n")
                                append("Invoice #${inv.invoiceNumber}\n")
                                append("Date: ${dateFormat.format(Date(inv.invoiceDate))}\n\n")
                                invoiceItems.forEach { item ->
                                    append("• ${item.itemName}: ₹${String.format(Locale.US, "%,.2f", item.totalAmount)}\n")
                                }
                                append("\nTotal: ₹${String.format(Locale.US, "%,.2f", inv.totalAmount)}\n")
                                append("Balance: ₹${String.format(Locale.US, "%,.2f", inv.totalAmount - inv.amountPaid)}\n\n")
                                append("Please make payment at your earliest. Thank you!")
                            }
                            val encoded = Uri.encode(message)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$encoded"))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08BD7C))
                ) {
                    Text("Reminder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    },
    content = { paddingValues ->
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
                .background(LightBlueBg)
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
                    Text(partyName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Status: ${inv.paymentStatus.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inv.paymentStatus == "paid") GreenBalance else RedAccent
                    )
                    if (inv.totalAmount - inv.amountPaid > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Balance Due: ₹${String.format(Locale.US, "%,.2f", inv.totalAmount - inv.amountPaid)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                    }
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
                        shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
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
                            shape = RoundedCornerShape(16.dp),
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
    )
}
