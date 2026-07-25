package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.ui.viewmodel.PartyViewModel
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDetailScreen(
    navController: NavController,
    partyId: Long = 0L,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    partyViewModel: PartyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val party by partyViewModel.currentParty.collectAsState()
    val allInvoices by invoiceViewModel.getInvoices().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(partyId) {
        partyViewModel.getPartyById(partyId)
    }

    val partyInvoices = allInvoices.filter { it.partyId == partyId }
    val totalReceivable = partyInvoices.filter { it.invoiceType == "sales" && it.paymentStatus != "paid" }.sumOf { it.totalAmount - it.amountPaid }
    val totalPayable = partyInvoices.filter { it.invoiceType == "purchase" && it.paymentStatus != "paid" }.sumOf { it.totalAmount - it.amountPaid }
    val partyBalance = totalReceivable - totalPayable

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val unpaidInvoices = partyInvoices.filter { it.paymentStatus != "paid" }
                        if (unpaidInvoices.isNotEmpty()) {
                            val message = buildString {
                                append("Hi ${party?.name ?: "Party"}, here's your account summary:\n\n")
                                unpaidInvoices.forEach { invoice ->
                                    val balance = invoice.totalAmount - invoice.amountPaid
                                    append("${invoice.invoiceNumber}: ₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)} (Balance: ₹${String.format(Locale.US, "%,.2f", balance)})\n")
                                }
                                append("\nTotal Due: ₹${String.format(Locale.US, "%,.2f", kotlin.math.abs(partyBalance))}\n\nPlease pay at your earliest convenience. Thank you!")
                            }
                            val encoded = android.net.Uri.encode(message)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/?text=$encoded"))
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share on WhatsApp", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = { navController.navigate(Screen.EditParty.createRoute(partyId)) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = VyaparTextPrimary,
                    navigationIconContentColor = VyaparTextPrimary,
                    actionIconContentColor = VyaparTextPrimary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.PaymentReceived.route) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, VyaparBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue)
                ) {
                    Text("Take Payment", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { navController.navigate(Screen.CreateInvoice.createRoute(partyId)) },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Invoice", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { navController.navigate(Screen.CreateInvoice.createRoute(partyId)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparRed)
                ) {
                    Text("Add Sale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VyaparBackground)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(party?.name ?: "Party", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Call, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(party?.phone ?: "No phone", fontSize = 14.sp, color = VyaparTextSecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Note, contentDescription = null, tint = if (partyBalance >= 0) VyaparGreen else VyaparRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (partyBalance >= 0) "Receivable:" else "Payable:", fontSize = 12.sp, color = VyaparTextSecondary)
                                }
                                Text(String.format(Locale.US, "\u20B9%,.2f", kotlin.math.abs(partyBalance)), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (partyBalance >= 0) VyaparGreen else VyaparRed)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OutlinedButton(onClick = { navController.navigate(Screen.PaymentReminders.route) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, VyaparBlue)) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Reminder", color = VyaparBlue, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(onClick = { navController.navigate(Screen.PartyStatement.createRoute(partyId)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, VyaparBlue)) {
                                Icon(Icons.Filled.Note, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Statement", color = VyaparBlue, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val tabs = listOf("Transactions", "Items")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(modifier = Modifier.weight(1f).clickable { selectedTab = index }.background(if (isSelected) VyaparSelectedBg else Color.Transparent).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) VyaparRed else VyaparTextSecondary)
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (partyInvoices.isEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No transactions yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to add a transaction", fontSize = 13.sp, color = VyaparTextSecondary)
                        }
                    }
                } else {
                    items(partyInvoices) { invoice ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id)) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = VyaparWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        when (invoice.invoiceType) {
                                            "sales" -> "Sale"
                                            "purchase" -> "Purchase"
                                            else -> invoice.invoiceType.replaceFirstChar { it.uppercase() }
                                        },
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(invoice.invoiceNumber, fontSize = 13.sp, color = VyaparTextSecondary)
                                        Text(dateFormat.format(Date(invoice.invoiceDate)), fontSize = 12.sp, color = VyaparTextSecondary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Total", fontSize = 11.sp, color = VyaparTextSecondary)
                                        Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Balance", fontSize = 11.sp, color = VyaparTextSecondary)
                                        Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount - invoice.amountPaid), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (invoice.paymentStatus == "paid") VyaparGreen else VyaparRed)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No items for this party", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap + to add an item", fontSize = 13.sp, color = VyaparTextSecondary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showDeleteDialog && party != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Party", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${party?.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    party?.let { partyViewModel.deleteParty(it) }
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = VyaparRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = VyaparBlue) }
            }
        )
    }
}
