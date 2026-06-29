package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(navController: NavController) {
    var partyName by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<InvoiceLineItem>()) }
    var notes by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var isIGST by remember { mutableStateOf(false) }

    val subTotal = items.sumOf { it.amount }
    val totalGst = items.sumOf { it.gstAmount }
    val grandTotal = subTotal + totalGst - (discount.toDoubleOrNull() ?: 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total: ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Rs. " + String.format(Locale.US, "%.2f", grandTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Save invoice */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save & Print", color = OnPrimary)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("Party Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = invoiceDate,
                    onValueChange = { invoiceDate = it },
                    label = { Text("Invoice Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(items.size) { index ->
                val item = items[index]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Qty: ${item.qty} x Rs. ${item.price} = Rs. ${item.amount}")
                            }
                            IconButton(onClick = {
                                items = items.toMutableList().also { it.removeAt(index) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                        Row {
                            Text(
                                text = "GST (${item.gstRate}%): Rs. " + String.format(Locale.US, "%.2f", item.gstAmount),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    items = items + InvoiceLineItem("New Item", 1.0, 100.0, 18.0)
                }, colors = ButtonDefaults.buttonColors(containerColor = Secondary)) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text("Add Item")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isIGST, onCheckedChange = { isIGST = it })
                    Text("Apply IGST (Inter-state)")
                }
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Terms & Conditions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    }
}

data class InvoiceLineItem(
    val name: String,
    var qty: Double,
    var price: Double,
    val gstRate: Double
) {
    val amount: Double get() = qty * price
    val gstAmount: Double get() = amount * gstRate / 100
}
