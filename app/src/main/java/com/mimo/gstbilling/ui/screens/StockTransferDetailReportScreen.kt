package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.util.Locale

data class TransferLineItem(
    val itemName: String,
    val quantity: Double,
    val unit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferDetailReportScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val fromWarehouse = remember { "Warehouse A" }
    val toWarehouse = remember { "Warehouse B" }
    val transferDate = remember { "15 Jul 2026" }
    val status = remember { "Completed" }

    val lineItems = remember {
        listOf(
            TransferLineItem("Widget A", 25.0, "pcs"),
            TransferLineItem("Widget B", 10.0, "kg"),
            TransferLineItem("Widget C", 5.0, "box"),
            TransferLineItem("Widget D", 50.0, "pcs")
        )
    }

    val totalItems = lineItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Transfer Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("From", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(fromWarehouse, fontSize = 13.sp, color = VyaparTextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("To", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(toWarehouse, fontSize = 13.sp, color = VyaparTextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(transferDate, fontSize = 13.sp, color = VyaparTextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status", fontSize = 13.sp, color = VyaparTextSecondary)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (status == "Completed") VyaparSuccessBackground else VyaparWarningBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = if (status == "Completed") VyaparGreen else VyaparOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        status,
                                        fontSize = 11.sp,
                                        color = if (status == "Completed") VyaparGreen else VyaparOrange,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Items", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text("${totalItems.toInt()} units (${lineItems.size} line items)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            item {
                Text("Line Items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }

            items(lineItems) { lineItem ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lineItem.itemName, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text("Unit: ${lineItem.unit}", fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${lineItem.quantity.toInt()} ${lineItem.unit}",
                                fontWeight = FontWeight.Bold,
                                color = VyaparBlue,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
