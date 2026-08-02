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
import com.mimo.gstbilling.data.local.entity.RawMaterialEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.RawMaterialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawMaterialScreen(navController: NavController, viewModel: RawMaterialViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editMaterial by remember { mutableStateOf<RawMaterialEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raw Materials", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary),
                actions = { IconButton(onClick = { editMaterial = null; showDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editMaterial = null; showDialog = true }, containerColor = RedAccent) {
                Icon(Icons.Filled.Add, contentDescription = "Add Material", tint = Color.White)
            }
        }
    ) { padding ->
        if (uiState.materials.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(80.dp), tint = VyaparTextSecondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No raw materials yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Text("Tap + to add a material", fontSize = 14.sp, color = VyaparTextSecondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(uiState.materials) { material ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { editMaterial = material; showDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Inventory, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(material.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("${material.unit} • HSN: ${material.hsnCode ?: "N/A"}", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%.2f", material.costPerUnit)}/${material.unit}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparBlue)
                                Text("Stock: ${material.stockQty.toInt()}", fontSize = 12.sp, color = if (material.stockQty < 10) VyaparRed else VyaparGreen)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf(editMaterial?.name ?: "") }
        var unit by remember { mutableStateOf(editMaterial?.unit ?: "kg") }
        var stockQty by remember { mutableStateOf(editMaterial?.stockQty?.toString() ?: "") }
        var costPerUnit by remember { mutableStateOf(editMaterial?.costPerUnit?.toString() ?: "") }
        var hsnCode by remember { mutableStateOf(editMaterial?.hsnCode ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editMaterial != null) "Edit Material" else "Add Raw Material", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Material Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg/meter/piece)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = stockQty, onValueChange = { stockQty = it }, label = { Text("Stock Quantity") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = costPerUnit, onValueChange = { costPerUnit = it }, label = { Text("Cost per Unit (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = hsnCode, onValueChange = { hsnCode = it }, label = { Text("HSN Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        if (editMaterial != null) {
                            viewModel.updateMaterial(editMaterial!!.copy(
                                name = name, unit = unit, stockQty = stockQty.toDoubleOrNull() ?: 0.0,
                                costPerUnit = costPerUnit.toDoubleOrNull() ?: 0.0, hsnCode = hsnCode.ifBlank { null }
                            ))
                        } else {
                            viewModel.addMaterial(name, unit, stockQty.toDoubleOrNull() ?: 0.0, costPerUnit.toDoubleOrNull() ?: 0.0, hsnCode.ifBlank { null })
                        }
                        showDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
