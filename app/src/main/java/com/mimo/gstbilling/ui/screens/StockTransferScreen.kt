package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mimo.gstbilling.ui.viewmodel.StockTransferViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(navController: NavController, viewModel: StockTransferViewModel = hiltViewModel()) {
    val transfers by viewModel.transfers.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val items by viewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (showAddDialog) {
        var fromWarehouseId by remember { mutableLongStateOf(0L) }
        var toWarehouseId by remember { mutableLongStateOf(0L) }
        var selectedItemId by remember { mutableLongStateOf(0L) }
        var selectedItemName by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var showFromDropdown by remember { mutableStateOf(false) }
        var showToDropdown by remember { mutableStateOf(false) }
        var showItemDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Stock Transfer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box {
                        OutlinedTextField(value = warehouses.find { it.id == fromWarehouseId }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("From Warehouse *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showFromDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showFromDropdown, onDismissRequest = { showFromDropdown = false }) {
                            warehouses.forEach { wh -> DropdownMenuItem(text = { Text(wh.name) }, onClick = { fromWarehouseId = wh.id; showFromDropdown = false }) }
                        }
                    }
                    Box {
                        OutlinedTextField(value = warehouses.find { it.id == toWarehouseId }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("To Warehouse *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showToDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showToDropdown, onDismissRequest = { showToDropdown = false }) {
                            warehouses.forEach { wh -> DropdownMenuItem(text = { Text(wh.name) }, onClick = { toWarehouseId = wh.id; showToDropdown = false }) }
                        }
                    }
                    Box {
                        OutlinedTextField(value = selectedItemName, onValueChange = {}, readOnly = true, label = { Text("Item *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showItemDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showItemDropdown, onDismissRequest = { showItemDropdown = false }) {
                            items.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { selectedItemId = item.id; selectedItemName = item.name; showItemDropdown = false }) }
                        }
                    }
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val q = qty.toDoubleOrNull() ?: 0.0
                    if (selectedItemName.isNotBlank() && q > 0 && fromWarehouseId > 0 && toWarehouseId > 0) {
                        viewModel.addTransfer(fromWarehouseId, toWarehouseId, selectedItemId, selectedItemName, q, notes.ifBlank { null })
                        showAddDialog = false
                    }
                }) { Text("Transfer") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stock Transfer", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A)))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = GreenBalance, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transfer")
            }
        }
    ) { padding ->
        if (transfers.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No transfers yet", fontSize = 16.sp, color = TextSecondary)
                Text("Tap + to transfer stock between warehouses", fontSize = 12.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transfers) { t ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Primary, modifier = Modifier.padding(8.dp).size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("${t.quantity.toInt()} pcs \u2022 ${dateFormat.format(Date(t.transferDate))}", fontSize = 12.sp, color = TextSecondary)
                                t.notes?.let { Text(it, fontSize = 11.sp, color = TextSecondary) }
                            }
                            IconButton(onClick = { viewModel.deleteTransfer(t) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
