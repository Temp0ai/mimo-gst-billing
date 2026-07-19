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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyStatementScreen(
    navController: NavController,
    partyId: Long,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    var partyName by remember { mutableStateOf("Loading...") }
    val allInvoices by viewModel.getInvoicesByParty(partyId).collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("All") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    LaunchedEffect(partyId) {
        partyName = viewModel.getPartyById(partyId)?.name ?: "Unknown Party"
    }

    val filteredInvoices = when (selectedFilter) {
        "This Month" -> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val monthStart = cal.timeInMillis
            allInvoices.filter { it.invoiceDate >= monthStart }
        }
        "This Year" -> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val yearStart = cal.timeInMillis
            allInvoices.filter { it.invoiceDate >= yearStart }
        }
        else -> allInvoices
    }

    val totalBilled = allInvoices.sumOf { it.totalAmount }
    val totalPaid = allInvoices.sumOf { it.amountPaid }
    val balanceDue = totalBilled - totalPaid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(partyName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Billed", fontSize = 11.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", totalBilled), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Paid", fontSize = 11.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", totalPaid), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenBalance)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Balance Due", fontSize = 11.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", balanceDue), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (balanceDue > 0) RedAccent else GreenBalance)
                            }
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "This Month", "This Year").forEach { filter ->
                        FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                    }
                }
            }
            items(filteredInvoices) { invoice ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            val bgColor = when (invoice.invoiceType) {
                                "sales" -> Primary.copy(alpha = 0.1f)
                                "purchase" -> GreenBalance.copy(alpha = 0.1f)
                                "credit_note" -> RedAccent.copy(alpha = 0.1f)
                                else -> TextSecondary.copy(alpha = 0.1f)
                            }
                            val iconTint = when (invoice.invoiceType) {
                                "sales" -> Primary
                                "purchase" -> GreenBalance
                                "credit_note" -> RedAccent
                                else -> TextSecondary
                            }
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
                                Icon(Icons.Filled.Receipt, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                val badgeColor = when (invoice.invoiceType) {
                                    "sales" -> Primary
                                    "purchase" -> GreenBalance
                                    "credit_note" -> RedAccent
                                    else -> TextSecondary
                                }
                                Card(shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f))) {
                                    Text(invoice.invoiceType.replace("_", " ").uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(dateFormat.format(Date(invoice.invoiceDate)), fontSize = 11.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            if (invoice.amountPaid > 0) {
                                Text("Paid: ${String.format(Locale.US, "\u20B9%,.2f", invoice.amountPaid)}", fontSize = 10.sp, color = GreenBalance)
                            }
                            val outstanding = invoice.totalAmount - invoice.amountPaid
                            if (outstanding > 0) {
                                Text("Due: ${String.format(Locale.US, "\u20B9%,.2f", outstanding)}", fontSize = 10.sp, color = RedAccent)
                            }
                        }
                    }
                }
            }
            item {
                if (filteredInvoices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Statement, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions found", fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
