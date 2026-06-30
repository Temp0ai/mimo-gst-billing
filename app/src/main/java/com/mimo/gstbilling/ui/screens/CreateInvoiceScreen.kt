package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

data class InvoiceLineItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val gstRate: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(navController: NavController) {
    var partyName by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf("30 Jun 2026") }
    var igstEnabled by remember { mutableStateOf(false) }
    var discount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val lineItems = remember {
        mutableStateOf(
            listOf(
                InvoiceLineItem("150ml R Cup", 100, 2.20, 18),
                InvoiceLineItem("2 Option Vending Machine", 1, 100000.0, 18)
            )
        )
    }

    val subtotal = lineItems.value.sumOf { it.quantity * it.price }
    val gstAmount = lineItems.value.sumOf { (it.quantity * it.price * it.gstRate) / 100.0 }
    val discountAmount = discount.toDoubleOrNull() ?: 0.0
    val total = subtotal + gstAmount - discountAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Invoice", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Party Name",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = partyName,
                                onValueChange = { partyName = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Select or enter party name") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Invoice Date",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = invoiceDate,
                                onValueChange = { invoiceDate = it },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = "Date")
                                },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                itemsIndexed(lineItems.value) { index, item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Qty: " + item.quantity + " x " + String.format(java.util.Locale.US, "%.2f", item.price),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "GST: " + item.gstRate + "%",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f", item.quantity * item.price),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            IconButton(
                                onClick = {
                                    val updated = lineItems.value.toMutableList()
                                    updated.removeAt(index)
                                    lineItems.value = updated
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = RedAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Item")
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = igstEnabled,
                                    onCheckedChange = { igstEnabled = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Text(
                                    text = "IGST (Integrated GST)",
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Discount",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = discount,
                                onValueChange = { discount = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter discount amount") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Notes",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Add any additional notes") },
                                shape = RoundedCornerShape(8.dp),
                                minLines = 2
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", subtotal),
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GST", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", gstAmount),
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    if (discountAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = "-" + String.format(java.util.Locale.US, "%.2f", discountAmount),
                                fontSize = 13.sp,
                                color = RedAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", total),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Save & Print",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
