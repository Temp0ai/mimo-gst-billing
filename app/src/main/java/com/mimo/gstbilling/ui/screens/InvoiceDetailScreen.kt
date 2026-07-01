package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

data class InvoiceItemDetail(
    val name: String,
    val qty: Double,
    val unit: String,
    val rate: Double,
    val discount: Double,
    val gstRate: Double,
    val total: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(navController: NavController) {
    val invoiceNo = "753"
    val date = "02/02/2026"
    val partyName = "Snapkeys"
    val phone = "+919356328245"
    val partyBalance = 15342.25

    val items = listOf(
        InvoiceItemDetail("CARDAMOM TEA PREMIX", 10.0, "Kg", 350.0, 0.0, 5.0, 3675.0),
        InvoiceItemDetail("COFFEE PREMIX", 2.0, "Kg", 350.0, 0.0, 18.0, 826.0)
    )

    val totalQty = items.sumOf { it.qty }
    val subtotal = items.sumOf { it.rate * it.qty }
    val totalDisc = items.sumOf { it.discount }
    val totalTax = items.sumOf { it.total - (it.rate * it.qty) }
    val grandTotal = items.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Sale", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = RedAccent, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueHeader)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Invoice No.", fontSize = 12.sp, color = TextSecondary)
                        Text(invoiceNo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Date", fontSize = 12.sp, color = TextSecondary)
                        Text(date, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Party Balance: ", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "\u20B9%,.2f", partyBalance),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenBalance
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = partyName,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer Name *") },
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("E-Way Bill No.") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenBalance)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Billed Items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("Rate exl. tax", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}  ${item.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "\u20B9%,.0f", item.total),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Subtotal", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                String.format(java.util.Locale.US, "%.0f %s x %.0f = \u20B9%,.0f", item.qty, item.unit, item.rate, item.rate * item.qty),
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount (%): ${item.discount.toInt()}", fontSize = 12.sp, color = RedAccent)
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.0f", item.discount),
                                fontSize = 12.sp,
                                color = RedAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax GST@${item.gstRate.toInt()}%: ${item.gstRate.toInt()}%", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.0f", item.total - (item.rate * item.qty)),
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Disc: ${String.format(java.util.Locale.US, "%.2f", totalDisc)}", fontSize = 13.sp, color = TextPrimary)
                            Text("Total Tax Amt: ${String.format(java.util.Locale.US, "%.2f", totalTax)}", fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Qty:${String.format(java.util.Locale.US, "%.1f", totalQty)}", fontSize = 13.sp, color = TextPrimary)
                            Text("Subtotal: ${String.format(java.util.Locale.US, "%.2f", grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
