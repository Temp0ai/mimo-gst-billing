package com.mimo.gstbilling.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentRemindersScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("all") }

    val pendingInvoices = invoices.filter {
        it.paymentStatus != "paid"
    }.map { invoice ->
        val party = parties.find { it.id == invoice.partyId }
        val balanceDue = invoice.totalAmount - invoice.amountPaid
        val daysOverdue = calculateDaysOverdue(invoice)
        InvoiceWithParty(invoice, party?.name ?: "Unknown", party?.phone, balanceDue, daysOverdue)
    }.sortedByDescending { it.daysOverdue }

    val filteredInvoices = when (selectedFilter) {
        "30+" -> pendingInvoices.filter { it.daysOverdue >= 30 }
        "60+" -> pendingInvoices.filter { it.daysOverdue >= 60 }
        "90+" -> pendingInvoices.filter { it.daysOverdue >= 90 }
        else -> pendingInvoices
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (pendingInvoices.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No pending payments", fontSize = 16.sp, color = TextSecondary)
                Text("All invoices are paid!", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
                // Summary card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RedAccent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pending Invoices", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${pendingInvoices.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem("All", selectedFilter == "all") { selectedFilter = "all" }
                    FilterChipItem("30+ days", selectedFilter == "30+") { selectedFilter = "30+" }
                    FilterChipItem("60+ days", selectedFilter == "60+") { selectedFilter = "60+" }
                    FilterChipItem("90+ days", selectedFilter == "90+") { selectedFilter = "90+" }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Invoice list
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInvoices) { item ->
                        InvoiceReminderItem(
                            item = item,
                            onWhatsAppClick = { openWhatsApp(context, item) },
                            onSmsClick = { openSms(context, item) }
                        )
                    }
                }

                // Send All Reminders button
                Button(
                    onClick = {
                        filteredInvoices.forEach { item ->
                            if (!item.phone.isNullOrBlank()) {
                                openSms(context, item)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(12.dp).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send All Reminders (${filteredInvoices.size})", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun InvoiceReminderItem(
    item: InvoiceWithParty,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit
) {
    val overdueColor = when {
        item.daysOverdue >= 90 -> RedAccent
        item.daysOverdue >= 60 -> Color(0xFFFF6D00)
        item.daysOverdue >= 30 -> Warning
        else -> TextSecondary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.partyName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Text(item.invoice.invoiceNumber, fontSize = 12.sp, color = TextSecondary)
                    Text(
                        "${item.daysOverdue} days overdue",
                        fontSize = 11.sp,
                        color = overdueColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Balance Due", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        formatCurrency(item.balanceDue),
                        fontWeight = FontWeight.Bold,
                        color = RedAccent,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.phone.isNullOrBlank()) {
                    TextButton(onClick = onWhatsAppClick) {
                        Text("WhatsApp", color = Color(0xFF25D366), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    TextButton(onClick = onSmsClick) {
                        Text("SMS", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                } else {
                    Text("No phone number", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

private fun calculateDaysOverdue(invoice: InvoiceEntity): Int {
    val now = System.currentTimeMillis()
    val invoiceDate = invoice.invoiceDate
    val diffMillis = now - invoiceDate
    return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(0)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "\u20B9%,.2f", amount)
}

private fun buildWhatsAppMessage(item: InvoiceWithParty): String {
    return "Hi ${item.partyName}, this is a friendly reminder for payment of Invoice #${item.invoice.invoiceNumber} " +
            "dated ${formatDate(item.invoice.invoiceDate)} for ${formatCurrency(item.invoice.totalAmount)}. " +
            "Balance due: ${formatCurrency(item.balanceDue)}. Please pay at your earliest convenience. Thank you!"
}

private fun buildSmsMessage(item: InvoiceWithParty): String {
    return "Reminder: Invoice #${item.invoice.invoiceNumber} for ${formatCurrency(item.invoice.totalAmount)}. " +
            "Balance due: ${formatCurrency(item.balanceDue)}. Please pay at your earliest."
}

private fun openWhatsApp(context: Context, item: InvoiceWithParty) {
    val phone = item.phone?.replace("[^0-9+]".toRegex(), "") ?: return
    val message = buildWhatsAppMessage(item)
    val url = "https://wa.me/$phone?text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

private fun openSms(context: Context, item: InvoiceWithParty) {
    val phone = item.phone?.replace("[^0-9+]".toRegex(), "") ?: return
    val message = buildSmsMessage(item)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phone")
        putExtra("sms_body", message)
    }
    context.startActivity(intent)
}

private data class InvoiceWithParty(
    val invoice: InvoiceEntity,
    val partyName: String,
    val phone: String?,
    val balanceDue: Double,
    val daysOverdue: Int
)
