package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

data class SaleInvoice(
    val invoiceNo: String,
    val partyName: String,
    val amount: Double,
    val date: String,
    val isPaid: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    val salesList = listOf(
        SaleInvoice("INV-001", "Dignitary Defence Academy", 45000.0, "30 Jun 2026", true),
        SaleInvoice("INV-002", "Sha Khimji & Premji Co", 28500.0, "28 Jun 2026", false),
        SaleInvoice("INV-003", "Sanman Enterprises", 115227.0, "25 Jun 2026", true),
        SaleInvoice("INV-004", "Adv. Pramod Bendre", 12500.0, "22 Jun 2026", true),
        SaleInvoice("INV-005", "Agad Logistics", 6380.0, "20 Jun 2026", false),
        SaleInvoice("INV-006", "Ishwar Heart Clinic", 35000.0, "18 Jun 2026", true),
        SaleInvoice("INV-007", "Taste of Irani", 7350.0, "15 Jun 2026", false)
    )

    val totalSales = salesList.sumOf { it.amount }
    val pendingAmount = salesList.filter { !it.isPaid }.sumOf { it.amount }
    val collectedAmount = salesList.filter { it.isPaid }.sumOf { it.amount }

    val filteredSales = if (searchQuery.isEmpty()) salesList
    else salesList.filter { it.partyName.contains(searchQuery, ignoreCase = true) || it.invoiceNo.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sale", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Filled.FilterList, contentDescription = "Filter") }
                    IconButton(onClick = { }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("View Report", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { navController.navigate(Screen.CreateInvoice.route) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Add Sale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Sale", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "\u20B9%,.0f", totalSales),
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collected", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "\u20B9%,.0f", collectedAmount),
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenBalance
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pending", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "\u20B9%,.0f", pendingAmount),
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SEARCH SALE", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(filteredSales) { sale ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clickable { navController.navigate(Screen.InvoiceDetail.createRoute(1L)) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sale.invoiceNo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(sale.partyName, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(sale.date, fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    String.format(java.util.Locale.US, "\u20B9%,.2f", sale.amount),
                                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (sale.isPaid) Icons.Filled.CheckCircle else Icons.Filled.Pending,
                                        contentDescription = null,
                                        tint = if (sale.isPaid) GreenBalance else RedAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (sale.isPaid) "Paid" else "Pending",
                                        fontSize = 12.sp,
                                        color = if (sale.isPaid) GreenBalance else RedAccent
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
