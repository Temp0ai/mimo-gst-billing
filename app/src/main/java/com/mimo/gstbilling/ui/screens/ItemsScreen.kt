package com.mimo.gstbilling.ui.screens

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
        confirmButton = { TextButton(onClick = { onAdd(name, hsn, price.toDoubleOrNull() ?: 0.0, 18.0, unit, 0.0) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class InventoryItem(val name: String, val hsnCode: String?, val salePrice: Double, val gstRate: Double, val unit: String, val stockQuantity: Double, val purchasePrice: Double = 0.0)
