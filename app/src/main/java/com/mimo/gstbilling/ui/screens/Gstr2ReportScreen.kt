package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class Gstr2Invoice(
    val invoiceNo: String,
    val party: String,
    val date: String,
    val taxable: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val total: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr2ReportScreen(navController: NavController) {
    var startDate by remember { mutableStateOf("01 Jul 2026") }
    var endDate by remember { mutableStateOf("31 Jul 2026") }
    var itcEligible by remember { mutableStateOf(true) }

    val invoices = remember {
        listOf(
            Gstr2Invoice("PUR-001", "Rahul Enterprises", "05 Jul 2026", 50000.0, 4500.0, 4500.0, 0.0, 59000.0),
            Gstr2Invoice("PUR-002", "Priya Traders", "12 Jul 2026", 35000.0, 3150.0, 3150.0, 0.0, 41300.0),
            Gstr2Invoice("PUR-003", "Amit & Sons", "18 Jul 2026", 22000.0, 0.0, 0.0, 3960.0, 25960.0),
            Gstr2Invoice("PUR-004", "Neha Distributors", "22 Jul 2026", 48000.0, 4320.0, 4320.0, 0.0, 56640.0),
            Gstr2Invoice("PUR-005", "Vikram Supply Co.", "25 Jul 2026", 15000.0, 1350.0, 1350.0, 0.0, 17700.0)
        )
    }

    val totalTaxable = invoices.sumOf { it.taxable }
    val totalCgst = invoices.sumOf { it.cgst }
    val totalSgst = invoices.sumOf { it.sgst }
    val totalIgst = invoices.sumOf { it.igst }
    val totalAmount = invoices.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-2 Report", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date Range", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("From") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("To") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ITC Eligible Only", fontSize = 14.sp, color = TextPrimary)
                            Switch(
                                checked = itcEligible,
                                onCheckedChange = { itcEligible = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Taxable", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalTaxable)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) }
                            Column { Text("CGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalCgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                            Column { Text("SGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalSgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                            Column { Text("IGST", fontSize = 12.sp, color = TextSecondary); Text("\u20B9${String.format("%,.0f", totalIgst)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("\u20B9${String.format("%,.0f", totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueHeader)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Invoice #", "Party", "Date", "Taxable", "CGST", "SGST", "IGST", "Total").forEach { col ->
                        Text(
                            col,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.width(70.dp)
                        )
                    }
                }
            }

            items(invoices) { inv ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(inv.invoiceNo, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(70.dp))
                        Text(inv.party, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(80.dp), maxLines = 1)
                        Text(inv.date, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.width(70.dp))
                        Text("\u20B9${String.format("%,.0f", inv.taxable)}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(70.dp))
                        Text("\u20B9${String.format("%,.0f", inv.cgst)}", fontSize = 12.sp, color = Primary, modifier = Modifier.width(60.dp))
                        Text("\u20B9${String.format("%,.0f", inv.sgst)}", fontSize = 12.sp, color = Primary, modifier = Modifier.width(60.dp))
                        Text("\u20B9${String.format("%,.0f", inv.igst)}", fontSize = 12.sp, color = Primary, modifier = Modifier.width(60.dp))
                        Text(
                            "\u20B9${String.format("%,.0f", inv.total)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.width(70.dp)
                        )
                    }
                }
            }
        }
    }
}
