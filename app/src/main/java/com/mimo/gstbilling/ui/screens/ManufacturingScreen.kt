package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.viewmodel.ManufacturingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManufacturingScreen(
    navController: NavController,
    viewModel: ManufacturingViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (showAddDialog) {
        AddManufacturingDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, qty, rawMaterials, cost, notes ->
                viewModel.addEntry(name, qty, rawMaterials, cost, notes)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manufacturing", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenBalance,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Manufacturing Entry")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueHeader),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Manufacturing Cost", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", totalCost),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Entries", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                entries.size.toString(),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No manufacturing entries yet", fontSize = 16.sp, color = TextSecondary)
                        Text("Tap + to add BOM entry", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.finishedItemName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Qty: ${entry.finishedItemQty.toInt()}", fontSize = 13.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Materials: ${entry.rawMaterials}", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(dateFormat.format(Date(entry.date)), fontSize = 11.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    String.format(Locale.US, "\u20B9%,.2f", entry.totalCost),
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                IconButton(onClick = { viewModel.deleteEntry(entry) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddManufacturingDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, qty: Double, rawMaterials: String, cost: Double, notes: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var rawMaterials by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manufacturing Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Finished Item *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rawMaterials, onValueChange = { rawMaterials = it }, label = { Text("Raw Materials (comma separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Total Cost *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && (qty.toDoubleOrNull() ?: 0.0) > 0 && (cost.toDoubleOrNull() ?: 0.0) > 0) {
                        onAdd(name, qty.toDoubleOrNull() ?: 1.0, rawMaterials, cost.toDoubleOrNull() ?: 0.0, notes.ifBlank { null })
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
