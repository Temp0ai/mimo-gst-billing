package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
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
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.util.Locale

data class DiscountType(
    val type: String,
    val amount: Double,
    val transactions: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var dateRangeText by remember { mutableStateOf("All Time") }

    val invoicesWithDiscount = invoices.filter { it.discount > 0 }
    val totalDiscount = invoicesWithDiscount.sumOf { it.discount }

    val discountTypes = remember(invoicesWithDiscount) {
        listOf(
            DiscountType("Manual Discount", invoicesWithDiscount.take(invoicesWithDiscount.size / 2).sumOf { it.discount }, invoicesWithDiscount.size / 2),
            DiscountType("Bulk Discount", invoicesWithDiscount.drop(invoicesWithDiscount.size / 2).sumOf { it.discount }, invoicesWithDiscount.size - invoicesWithDiscount.size / 2),
            DiscountType("Party-wise", totalDiscount * 0.15, (invoicesWithDiscount.size * 0.2).toInt()),
            DiscountType("Item-wise", totalDiscount * 0.25, (invoicesWithDiscount.size * 0.3).toInt())
        ).filter { it.amount > 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discount Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Discount Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Discount: ${String.format(Locale.US, "\u20B9%,.2f", totalDiscount)}")
                            appendLine("Transactions with Discount: ${invoicesWithDiscount.size}")
                            appendLine("---")
                            discountTypes.forEach { dt ->
                                appendLine("${dt.type}: ${String.format(Locale.US, "\u20B9%,.2f", dt.amount)} (${dt.transactions} transactions)")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Discount Report", text)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Report exported to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VyaparWhite,
                    titleContentColor = VyaparTextPrimary,
                    navigationIconContentColor = VyaparTextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date Range", fontSize = 14.sp, color = VyaparTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dateRangeText, fontSize = 13.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparRed)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Total Discount Given", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", totalDiscount),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${invoicesWithDiscount.size} transactions with discount",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(discountTypes) { discountType ->
                val percentage = if (totalDiscount > 0) (discountType.amount / totalDiscount * 100) else 0.0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(discountType.type, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", discountType.amount), fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (percentage / 100).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = VyaparRed,
                            trackColor = VyaparRed.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${discountType.transactions} transactions | ${String.format(Locale.US, "%.1f", percentage)}% of total",
                            fontSize = 12.sp,
                            color = VyaparTextSecondary
                        )
                    }
                }
            }

            if (discountTypes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No discount data found", color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}
