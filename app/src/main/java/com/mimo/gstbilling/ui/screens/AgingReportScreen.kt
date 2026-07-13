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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.util.*

data class AgingBucket(val label: String, val amount: Double, val color: Color, val invoiceCount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgingReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }

    val now = Calendar.getInstance().timeInMillis
    val dayMs = 86400000L

    val receivableAging = remember(invoices) {
        val unpaidSales = invoices.filter { it.invoiceType == "sales" && it.paymentStatus != "paid" }
        val buckets = mutableListOf<AgingBucket>()
        val current = unpaidSales.filter { now - it.invoiceDate < dayMs * 1 }
        val d1_30 = unpaidSales.filter { val age = now - it.invoiceDate; age in dayMs * 1 until dayMs * 31 }
        val d31_60 = unpaidSales.filter { val age = now - it.invoiceDate; age in dayMs * 31 until dayMs * 61 }
        val d61_90 = unpaidSales.filter { val age = now - it.invoiceDate; age in dayMs * 61 until dayMs * 91 }
        val d90plus = unpaidSales.filter { now - it.invoiceDate >= dayMs * 91 }
        buckets.add(AgingBucket("Current (0 days)", current.sumOf { it.totalAmount }, GreenBalance, current.size))
        buckets.add(AgingBucket("1-30 days", d1_30.sumOf { it.totalAmount }, Color(0xFFFFC107), d1_30.size))
        buckets.add(AgingBucket("31-60 days", d31_60.sumOf { it.totalAmount }, Color(0xFFFF9800), d31_60.size))
        buckets.add(AgingBucket("61-90 days", d61_90.sumOf { it.totalAmount }, RedAccent, d61_90.size))
        buckets.add(AgingBucket("90+ days", d90plus.sumOf { it.totalAmount }, Color(0xFFD32F2F), d90plus.size))
        buckets
    }

    val payableAging = remember(invoices) {
        val unpaidPurchases = invoices.filter { it.invoiceType == "purchase" && it.paymentStatus != "paid" }
        val buckets = mutableListOf<AgingBucket>()
        val current = unpaidPurchases.filter { now - it.invoiceDate < dayMs * 1 }
        val d1_30 = unpaidPurchases.filter { val age = now - it.invoiceDate; age in dayMs * 1 until dayMs * 31 }
        val d31_60 = unpaidPurchases.filter { val age = now - it.invoiceDate; age in dayMs * 31 until dayMs * 61 }
        val d61_90 = unpaidPurchases.filter { val age = now - it.invoiceDate; age in dayMs * 61 until dayMs * 91 }
        val d90plus = unpaidPurchases.filter { now - it.invoiceDate >= dayMs * 91 }
        buckets.add(AgingBucket("Current (0 days)", current.sumOf { it.totalAmount }, GreenBalance, current.size))
        buckets.add(AgingBucket("1-30 days", d1_30.sumOf { it.totalAmount }, Color(0xFFFFC107), d1_30.size))
        buckets.add(AgingBucket("31-60 days", d31_60.sumOf { it.totalAmount }, Color(0xFFFF9800), d31_60.size))
        buckets.add(AgingBucket("61-90 days", d61_90.sumOf { it.totalAmount }, RedAccent, d61_90.size))
        buckets.add(AgingBucket("90+ days", d90plus.sumOf { it.totalAmount }, Color(0xFFD32F2F), d90plus.size))
        buckets
    }

    val data = if (selectedTab == 0) receivableAging else payableAging
    val totalAmount = data.sumOf { it.amount }
    val totalInvoices = data.sumOf { it.invoiceCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aging Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Receivable", "Payable").forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (index == 0) GreenBalance else RedAccent,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total ${if (selectedTab == 0) "Receivable" else "Payable"}", fontSize = 14.sp, color = TextSecondary)
                    Text(String.format(java.util.Locale.US, "\u20B9%,.2f", totalAmount), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) GreenBalance else RedAccent)
                    Text("$totalInvoices unpaid invoices", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(data.size) { index ->
                    val bucket = data[index]
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(bucket.color, RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bucket.label, fontSize = 14.sp, color = TextPrimary)
                                Text("${bucket.invoiceCount} invoices", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(String.format(java.util.Locale.US, "\u20B9%,.2f", bucket.amount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
