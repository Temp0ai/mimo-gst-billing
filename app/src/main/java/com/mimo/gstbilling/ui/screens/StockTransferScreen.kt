package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.mimo.gstbilling.ui.viewmodel.StockTransferViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(navController: NavController, viewModel: StockTransferViewModel = hiltViewModel()) {
    val transfers by viewModel.transfers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (showAddDialog) {
        var fromStore by remember { mutableStateOf("") }
        var toStore by remember { mutableStateOf("") }
        var itemName by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Stock Transfer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fromStore, onValueChange = { fromStore = it }, label = { Text("From Store *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = toStore, onValueChange = { toStore = it }, label = { Text("To Store *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (itemName.isNotBlank() && qty.toDoubleOrNull() != null) {
                        viewModel.addTransfer(1L, 2L, itemName, qty.toDoubleOrNull() ?: 0.0, "Pcs", notes.ifBlank { null })
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
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
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transfers) { t ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.itemName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${t.quantity.toInt()} ${t.unit} \u2022 ${dateFormat.format(Date(t.date))}", fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { viewModel.deleteTransfer(t) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
