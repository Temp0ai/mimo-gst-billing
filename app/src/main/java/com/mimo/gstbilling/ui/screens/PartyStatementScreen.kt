package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.mimo.gstbilling.ui.viewmodel.PartyViewModel
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyStatementScreen(
    navController: NavController,
    partyId: Long = 0L,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    partyViewModel: PartyViewModel = hiltViewModel()
) {
    val invoices by invoiceViewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val parties by partyViewModel.parties.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    var selectedPartyId by remember { mutableStateOf(partyId) }
    var selectedParty by remember { mutableStateOf<PartyEntity?>(null) }
    var showPartyPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        partyViewModel.loadParties(1L)
    }

    LaunchedEffect(selectedPartyId) {
        selectedParty = parties.find { it.id == selectedPartyId }
    }

    val partyInvoices = invoices.filter { it.partyId == selectedPartyId }
    val totalAmount = partyInvoices.sumOf { it.totalAmount }
    val totalPaid = partyInvoices.sumOf { it.amountPaid }
    val balance = totalAmount - totalPaid

    if (showPartyPicker) {
        AlertDialog(
            onDismissRequest = { showPartyPicker = false },
            title = { Text("Select Party", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn {
                    items(parties) { party ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedPartyId = party.id
                                showPartyPicker = false
                            }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(party.name, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(party.phone ?: "", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPartyPicker = false }) { Text("Cancel", color = Primary) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Party Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp).clickable { showPartyPicker = true }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedParty?.name ?: "Select a party", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(selectedParty?.phone ?: "Tap to choose party", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
            if (selectedPartyId > 0) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BlueHeader)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column { Text("Total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalAmount), color = Color.White, fontWeight = FontWeight.Bold) }
                                Column(horizontalAlignment = Alignment.End) { Text("Paid", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalPaid), color = Color.White, fontWeight = FontWeight.Bold) }
                                Column(horizontalAlignment = Alignment.End) { Text("Balance", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", balance), color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
            if (partyInvoices.isEmpty()) {
                item { Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("No transactions for this party", fontSize = 14.sp, color = TextSecondary) } }
            } else {
                items(partyInvoices) { inv ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
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
