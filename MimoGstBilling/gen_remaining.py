import os

project_dir = "/root/Downloads/mimo ai/MimoGstBilling/app/src/main/java/com/mimo/gstbilling"

# 1. PartiesScreen.kt
with open(f"{project_dir}/ui/screens/PartiesScreen.kt", "w") as f:
    f.write("""package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val parties = remember { mutableStateListOf(
        Party("ABC Enterprises", "9876543210", "GSTIN1234567890", "Customer", "Rs. 12,450 Cr"),
        Party("XYZ Traders", "8765432109", "GSTIN0987654321", "Supplier", "Rs. 5,200 Dr"),
        Party("Global Solutions", "7654321098", "GSTIN1122334455", "Customer", "Rs. 25,000 Cr"),
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Secondary) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text("Search parties") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(parties.size) { index ->
                    val party = parties[index]
                    PartyCard(party)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddPartyDialog(onDismiss = { showAddDialog = false }, onAdd = { name, phone, gstin, type ->
            parties.add(Party(name, phone, gstin, type, "Rs. 0.00"))
            showAddDialog = false
        })
    }
}

@Composable
private fun PartyCard(party: Party) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = "Party", tint = Primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(party.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(party.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(party.gstin ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (party.type == "Customer") {
                    Text(party.type, style = MaterialTheme.typography.bodySmall, color = Success)
                } else {
                    Text(party.type, style = MaterialTheme.typography.bodySmall, color = Error)
                }
            }
            Text(party.balance, fontWeight = FontWeight.Bold, color = if (party.balance.contains("Cr")) Success else Error)
        }
    }
}

@Composable
private fun AddPartyDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Customer") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Party") },
        text = {
            Column {
                OutlinedTextField(value = Marvin, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gstin, onValueChange = { gstin = it }, label = { Text("GSTIN") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, phone, gstin, type) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class Party(val name: String, val phone: String, val gstin: String?, val type: String, val balance: String)
""")

# 2. ItemsScreen.kt
with open(f"{project_dir}/ui/screens/ItemsScreen.kt", "w") as f:
    f.write("""package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    val items = remember { mutableStateListOf(
        InventoryItem("Laptop Dell XPS", "8471", 65000.0, 18.0, "Pcs", 10.0),
        InventoryItem("Wireless Mouse", "8472", 500.0, 18.0, "Pcs", 50.0),
        InventoryItem("Mechanical Keyboard", "8473", 3500.0, 18.0, "Pcs", 25.0),
        InventoryItem("Monitor 27 inch", "8528", 25000.0, 28.0, "Pcs", 15.0),
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Items & Services", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Secondary) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            items(items.size) { index ->
                val item = items[index]
                ItemCard(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(onDismiss = { showAddDialog = false }, onAdd = { name, hsn, price, gst, unit, stock ->
            items.add(InventoryItem(name, hsn, price, gst, unit, stock))
            showAddDialog = false
        })
    }
}

@Composable
private fun ItemCard(item: InventoryItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("HSN: " + (item.hsnCode ?: "N/A"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rs. " + String.format("%.2f", item.salePrice), fontWeight = FontWeight.Bold, color = Primary)
                    Text("GST: " + item.gstRate + "%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("Stock: " + item.stockQuantity + " " + item.unit, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text("Purchase: Rs. " + String.format("%.2f", item.purchasePrice), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AddItemDialog(onDismiss: () -> Unit, onAdd: (String, String?, Double, Double, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hsn by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Pcs") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = hsn, onValueChange = { hsn = it }, label = { Text("HSN Code") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Sale Price*") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, hsn, price.toDoubleOrNull() ?: 0.0, 18.0, unit, 0.0)}) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class InventoryItem(val name: String, val hsnCode: String?, val salePrice: Double, val gstRate: Double, val unit: String, val stockQuantity: Double, val purchasePrice: Double = 0.0)
""")

# 3. ReportsScreen.kt
with open(f"{project_dir}/ui/screens/ReportsScreen.kt", "w") as f:
    f.write("""package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & GST", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding�(16.dp)) {
            item { Text("GST Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("GSTR-1 (Outward)", "Rs. 1,24,500", "5 invoices", Success) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("GSTR-3B Summary", "Rs. 45,200", "Monthly return", Primary) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Sales & Purchase", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("Sales Report", "Rs. 1,24,500", "12 transactions", Success) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("Purchase Report", "Rs. 45,200", "8 transactions", Error) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Party Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("Party Ledger", "Rs. 34,800", "3 active parties", Primary) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("Stock Report", "Rs. 2,50,000", "45 items", Warning) }
        }
    }
}

@Composable
private fun ReportCard(title: String, amount: String, subtitle: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(amount, fontWeight = FontonenWeight.Bold, color = color)
        }
    }
}
""")

# 4. SettingsScreen.kt
with open(f"{project_dir}/ui/screens/SettingsScreen.kt", "w") as f:#!/bin/bash
echo "Script created. Run it now:"
