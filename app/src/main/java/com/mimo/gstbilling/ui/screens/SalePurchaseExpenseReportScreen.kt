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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalePurchaseExpenseReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val expenses by viewModel.getExpenses().collectAsState(initial = emptyList())
    var dateRangeText by remember { mutableStateOf("All Time") }

    val totalSales = invoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
    val totalPurchases = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalSales - totalPurchases - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sale/Purchase/Expense Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Sale/Purchase/Expense Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Sales: ${String.format(Locale.US, "\u20B9%,.2f", totalSales)}")
                            appendLine("Total Purchases: ${String.format(Locale.US, "\u20B9%,.2f", totalPurchases)}")
                            appendLine("Total Expenses: ${String.format(Locale.US, "\u20B9%,.2f", totalExpenses)}")
                            appendLine("Net Profit: ${String.format(Locale.US, "\u20B9%,.2f", netProfit)}")
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Sale Purchase Expense Report", text)
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
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sales", fontSize = 14.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalSales), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Purchases", fontSize = 14.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalPurchases), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Expenses", fontSize = 14.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalExpenses), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = VyaparDivider)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Profit/Loss", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VyaparTextPrimary)
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", netProfit),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (netProfit >= 0) VyaparGreen else VyaparRed
                            )
                        }
                    }
                }
            }

            item {
                Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }

            val recentSales = invoices.filter { it.invoiceType == "sales" }.take(5)
            items(recentSales) { inv ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text("Sale", fontSize = 12.sp, color = VyaparGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontWeight = FontWeight.Bold, color = VyaparGreen)
                            Text(inv.paymentStatus.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = if (inv.paymentStatus == "paid") VyaparGreen else VyaparRed)
                        }
                    }
                }
            }

            if (recentSales.isEmpty() && expenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}
