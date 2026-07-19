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
import com.mimo.gstbilling.data.local.entity.WarehouseEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreManagementScreen(navController: NavController, viewModel: WarehouseViewModel = hiltViewModel()) {
    val warehouses by viewModel.warehouses.collectAsState()
    val transfers by viewModel.stockTransfers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWarehouse by remember { mutableStateOf<WarehouseEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<WarehouseEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    if (showDeleteConfirm != null) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = null }, title = { Text("Delete Warehouse", fontWeight = FontWeight.Bold) }, text = { Text("Delete ${showDeleteConfirm!!.name}? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteWarehouse(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } })
    }

    if (showAddDialog) {
        AddEditWarehouseDialog(warehouse = editingWarehouse, onDismiss = { showAddDialog = false; editingWarehouse = null }, onSave = { name, address, phone, manager ->
            if (editingWarehouse != null) viewModel.editWarehouse(editingWarehouse!!.copy(name = name, address = address, phone = phone, managerName = manager))
            else viewModel.addWarehouse(name, address, phone, manager)
            showAddDialog = false; editingWarehouse = null
        })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stores & Warehouses", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingWarehouse = null; showAddDialog = true }, containerColor = Primary, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Add Warehouse")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Warehouses (${warehouses.size})") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Transfers (${transfers.size})") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
            }
            if (selectedTab == 0) {
                LazyColumn {
                    items(warehouses, key = { it.id }) { warehouse ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { editingWarehouse = warehouse; showAddDialog = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (warehouse.isDefault) GreenBalance.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f))) {
                                        Icon(Icons.Filled.Store, contentDescription = null, tint = if (warehouse.isDefault) GreenBalance else Primary, modifier = Modifier.padding(10.dp).size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(warehouse.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        if (warehouse.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Card(shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance.copy(alpha = 0.1f))) {
                                                Text("Default", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenBalance, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    warehouse.address?.let { Text(it, fontSize = 12.sp, color = TextSecondary) }
                                    warehouse.managerName?.let { Text("Manager: $it", fontSize = 11.sp, color = TextSecondary) }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (!warehouse.isDefault) {
                                        TextButton(onClick = { viewModel.setDefault(warehouse) }, contentPadding = PaddingValues(0.dp)) { Text("Set Default", fontSize = 10.sp, color = Primary) }
                                    }
                                    IconButton(onClick = { showDeleteConfirm = warehouse }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { if (warehouses.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Store, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No warehouses yet", fontSize = 14.sp, color = TextSecondary); Text("Tap + to add your first store", fontSize = 12.sp, color = TextSecondary) } } } }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn {
                    items(transfers) { transfer ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) {
                                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Primary, modifier = Modifier.padding(8.dp).size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${transfer.itemName} (${transfer.quantity})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Transfer", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                    item { if (transfers.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No transfers yet", color = TextSecondary) } } }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWarehouseDialog(warehouse: WarehouseEntity?, onDismiss: () -> Unit, onSave: (String, String?, String?, String?) -> Unit) {
    var name by remember { mutableStateOf(warehouse?.name ?: "") }
    var address by remember { mutableStateOf(warehouse?.address ?: "") }
    var phone by remember { mutableStateOf(warehouse?.phone ?: "") }
    var manager by remember { mutableStateOf(warehouse?.managerName ?: "") }
    val isEditing = warehouse != null

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (isEditing) "Edit Warehouse" else "Add Warehouse", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Warehouse Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = manager, onValueChange = { manager = it }, label = { Text("Manager Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name, address.ifBlank { null }, phone.ifBlank { null }, manager.ifBlank { null }) }, enabled = name.isNotBlank()) { Text(if (isEditing) "Update" else "Add", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
