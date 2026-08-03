package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Vat201ReturnScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by invoiceViewModel.getInvoices().collectAsState(initial = emptyList())
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    val totalSales = invoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
    val totalOutputTax = invoices.filter { it.invoiceType == "sales" }.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    val totalPurchases = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
    val totalInputTax = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    val netVatPayable = totalOutputTax - totalInputTax

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VAT 201 Return (Legacy)", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VAT Summary - $currentYear", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Sales", fontSize = 14.sp); Text("₹${String.format("%,.2f", totalSales)}", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Output Tax", fontSize = 14.sp); Text("₹${String.format("%,.2f", totalOutputTax)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparRed) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Purchases", fontSize = 14.sp); Text("₹${String.format("%,.2f", totalPurchases)}", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Input Tax", fontSize = 14.sp); Text("₹${String.format("%,.2f", totalInputTax)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparGreen) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Net VAT Payable", fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("₹${String.format("%,.2f", netVatPayable)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (netVatPayable > 0) VyaparRed else VyaparGreen) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Note: VAT 201 was replaced by GST GSTR-3B from July 2017. This screen shows legacy reference data from your invoices.", fontSize = 12.sp, color = VyaparTextSecondary)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
