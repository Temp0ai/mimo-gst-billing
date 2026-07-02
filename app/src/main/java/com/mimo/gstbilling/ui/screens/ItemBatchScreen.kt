package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import java.util.Locale
import com.mimo.gstbilling.ui.viewmodel.ItemBatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBatchScreen(navController: NavController, viewModel: ItemBatchViewModel = hiltViewModel()) {
    val batches by viewModel.batches.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var itemName by remember { mutableStateOf("") }
        var batchNo by remember { mutableStateOf("") }
        var serialNo by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var purchasePrice by remember { mutableStateOf("") }
        var salePrice by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Batch/Serial", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = batchNo, onValueChange = { batchNo = it }, label = { Text("Batch Number *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = serialNo, onValueChange = { serialNo = it }, label = { Text("Serial Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Purchase Price") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (itemName.isNotBlank() && batchNo.isNotBlank()) {
                        viewModel.addBatch(1L, itemName, batchNo, serialNo.ifBlank { null }, null, qty.toDoubleOrNull() ?: 1.0, purchasePrice.toDoubleOrNull() ?: 0.0, salePrice.toDoubleOrNull() ?: 0.0, null)
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Item Batch/Serial Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = GreenBalance, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Add Batch")
            }
        }
    ) { padding ->
        if (batches.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("No batch/serial data yet", fontSize = 16.sp, color = TextSecondary)
                Text("Tap + to add batch tracking", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(batches) { batch ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(batch.itemName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Batch: ${batch.batchNumber}", fontSize = 12.sp, color = TextSecondary)
                                batch.serialNumber?.let { Text("Serial: $it", fontSize = 12.sp, color = TextSecondary) }
                                Text("Qty: ${batch.quantity.toInt()}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", batch.salePrice), fontWeight = FontWeight.Bold, color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
