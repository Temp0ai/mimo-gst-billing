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
import com.mimo.gstbilling.data.local.entity.BillOfMaterialsEntity
import com.mimo.gstbilling.data.local.entity.BomItemEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.BomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManufacturingScreen(navController: NavController, viewModel: BomViewModel = hiltViewModel()) {
    val boms by viewModel.boms.collectAsState()
    val allItems by viewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<BillOfMaterialsEntity?>(null) }
    var expandedBom by remember { mutableStateOf<Long?>(null) }

    if (showDeleteConfirm != null) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = null }, title = { Text("Delete BOM", fontWeight = FontWeight.Bold) }, text = { Text("Delete '${showDeleteConfirm!!.name}'?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteBom(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } })
    }

    if (showAddDialog) {
        var bomName by remember { mutableStateOf("") }
        var outputItemName by remember { mutableStateOf("") }
        var outputItemId by remember { mutableLongStateOf(0L) }
        var outputQty by remember { mutableStateOf("1") }
        var notes by remember { mutableStateOf("") }
        var showItemDropdown by remember { mutableStateOf(false) }

        AlertDialog(onDismissRequest = { showAddDialog = false },
            title = { Text("Create BOM", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = bomName, onValueChange = { bomName = it }, label = { Text("BOM Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Box {
                        OutlinedTextField(value = outputItemName, onValueChange = {}, readOnly = true, label = { Text("Output Item *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showItemDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showItemDropdown, onDismissRequest = { showItemDropdown = false }) {
                            allItems.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { outputItemName = item.name; outputItemId = item.id; showItemDropdown = false }) }
                        }
                    }
                    OutlinedTextField(value = outputQty, onValueChange = { outputQty = it }, label = { Text("Output Quantity") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = {
                if (bomName.isNotBlank() && outputItemId > 0) {
                    viewModel.addBom(bomName, outputItemId, outputItemName, outputQty.toDoubleOrNull() ?: 1.0, notes.ifBlank { null }, emptyList())
                    showAddDialog = false
                }
            }, enabled = bomName.isNotBlank() && outputItemId > 0) { Text("Create", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Manufacturing", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Primary, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add BOM") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            items(boms, key = { it.id }) { bom ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))) {
                                Icon(Icons.Filled.Build, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bom.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("Output: ${bom.outputItemName} x ${bom.outputQuantity}", fontSize = 12.sp, color = TextSecondary)
                        }
                        IconButton(onClick = { showDeleteConfirm = bom }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            item { if (boms.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Build, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No BOMs yet", fontSize = 14.sp, color = TextSecondary); Text("Create a Bill of Materials to start manufacturing", fontSize = 12.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
