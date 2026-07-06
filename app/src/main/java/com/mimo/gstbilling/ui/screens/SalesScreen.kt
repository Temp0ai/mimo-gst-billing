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
fun SalesScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val tabs = listOf("All", "Paid", "Unpaid", "Partial")
    val filteredInvoices = when (selectedTab) {
        1 -> invoices.filter { it.paymentStatus == "paid" }
        2 -> invoices.filter { it.paymentStatus == "unpaid" }
        3 -> invoices.filter { it.paymentStatus == "partial" }
        else -> invoices
    }
    val totalSales = invoices.sumOf { it.totalAmount }
    val collected = invoices.filter { it.paymentStatus == "paid" }.sumOf { it.totalAmount }
    val pending = totalSales - collected

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sale", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { navController.navigate(Screen.CashBank.route) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(25.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Primary), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)) { Text("Take Payment", fontSize = 13.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { navController.navigate(Screen.CreateInvoice.route) }, modifier = Modifier.size(50.dp), shape = androidx.compose.foundation.shape.CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Primary), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White) }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { navController.navigate(Screen.CreateInvoice.route) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) { Text("Add Sale", fontSize = 13.sp) }
            }
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
                        Column { Text("Total Sales", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalSales), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
                        Column(horizontalAlignment = Alignment.End) { Text("Collected", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", collected), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenBalance) }
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
            item { if (filteredInvoices.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No invoices found", fontSize = 14.sp, color = TextSecondary) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
