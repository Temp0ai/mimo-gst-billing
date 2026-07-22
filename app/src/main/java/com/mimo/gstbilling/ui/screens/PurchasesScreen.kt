package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val tabs = listOf("All", "Paid", "Unpaid", "Partial")
    val filteredInvoices = when (selectedTab) {
        1 -> invoices.filter { it.paymentStatus == "paid" }
        2 -> invoices.filter { it.paymentStatus == "unpaid" }
        3 -> invoices.filter { it.paymentStatus == "partial" }
        else -> invoices
    }
    val totalPurchases = invoices.sumOf { it.totalAmount }
    val paid = invoices.filter { it.paymentStatus == "paid" }.sumOf { it.totalAmount }
    val pending = totalPurchases - paid

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Purchase", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(modifier = Modifier.weight(1f).clickable { selectedTab = index }.background(if (isSelected) Color(0xFFFFEBEE) else Color.Transparent).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) RedAccent else TextSecondary)
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Total", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalPurchases), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
                        Column(horizontalAlignment = Alignment.End) { Text("Paid", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", paid), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenBalance) }
                        Column(horizontalAlignment = Alignment.End) { Text("Pending", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", pending), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent) }
                    }
                }
            }
            items(filteredInvoices) { invoice ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id)) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary); Text(dateFormat.format(Date(invoice.invoiceDate)), fontSize = 12.sp, color = TextSecondary) }
                        Column(horizontalAlignment = Alignment.End) { Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, color = TextPrimary); Text(invoice.paymentStatus.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (invoice.paymentStatus == "paid") GreenBalance else RedAccent) }
                    }
                }
            }
            item { if (filteredInvoices.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(12.dp)); Text("No purchases yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary); Spacer(modifier = Modifier.height(4.dp)); Text("Tap + to create a purchase", fontSize = 13.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
