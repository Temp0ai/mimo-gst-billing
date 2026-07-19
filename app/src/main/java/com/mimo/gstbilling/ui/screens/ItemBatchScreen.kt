package com.mimo.gstbilling.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemBatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBatchScreen(navController: NavController, viewModel: ItemBatchViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val batches by viewModel.batches.collectAsState()
    val items by viewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.ItemBatchEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    if (showDeleteConfirm != null) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = null }, title = { Text("Delete Batch", fontWeight = FontWeight.Bold) }, text = { Text("Delete batch '${showDeleteConfirm!!.batchNumber}'?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteBatch(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } })
    }

    if (showAddDialog) {
        var selectedItemId by remember { mutableLongStateOf(0L) }
        var selectedItemName by remember { mutableStateOf("") }
        var batchNumber by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var purchasePrice by remember { mutableStateOf("") }
        var mfgDate by remember { mutableStateOf<Long?>(null) }
        var expiryDate by remember { mutableStateOf<Long?>(null) }
        var showItemDropdown by remember { mutableStateOf(false) }

        AlertDialog(onDismissRequest = { showAddDialog = false },
            title = { Text("Add Batch", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box {
                        OutlinedTextField(value = selectedItemName, onValueChange = {}, readOnly = true, label = { Text("Item *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showItemDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showItemDropdown, onDismissRequest = { showItemDropdown = false }) {
                            items.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { selectedItemId = item.id; selectedItemName = item.name; showItemDropdown = false }) }
                        }
                    }
                    OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Batch Number *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity *") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Purchase Price") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mfgDate?.let { dateFormat.format(Date(it)) } ?: "", onValueChange = {}, readOnly = true, label = { Text("Manufacturing Date") }, trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick", modifier = Modifier.clickable { val c = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> val cal = Calendar.getInstance(); cal.set(y, m, d); mfgDate = cal.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expiryDate?.let { dateFormat.format(Date(it)) } ?: "", onValueChange = {}, readOnly = true, label = { Text("Expiry Date") }, trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick", modifier = Modifier.clickable { val c = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> val cal = Calendar.getInstance(); cal.set(y, m, d); expiryDate = cal.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }) }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = {
                val qty = quantity.toDoubleOrNull() ?: 0.0
                if (selectedItemId > 0 && batchNumber.isNotBlank() && qty > 0) {
                    viewModel.addBatch(selectedItemId, selectedItemName, batchNumber, qty, mfgDate, expiryDate, purchasePrice.toDoubleOrNull() ?: 0.0)
                    showAddDialog = false
                }
            }, enabled = selectedItemId > 0 && batchNumber.isNotBlank() && (quantity.toDoubleOrNull() ?: 0.0) > 0) { Text("Add", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Batch Tracking", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Primary, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add Batch") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            items(batches, key = { it.id }) { batch ->
                val isExpired = batch.expiryDate?.let { it < System.currentTimeMillis() } ?: false
                val isExpiringSoon = batch.expiryDate?.let { it - System.currentTimeMillis() < 30L * 24 * 60 * 60 * 1000 && it > System.currentTimeMillis() } ?: false
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            val bgColor = if (isExpired) RedAccent.copy(alpha = 0.1f) else if (isExpiringSoon) Color(0xFFF79F1F).copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f)
                            val iconTint = if (isExpired) RedAccent else if (isExpiringSoon) Color(0xFFF79F1F) else Primary
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
                                Icon(Icons.Filled.QrCode2, contentDescription = null, tint = iconTint, modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(batch.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Batch: ${batch.batchNumber}", fontSize = 12.sp, color = Primary)
                            Row {
                                Text("Qty: ${batch.quantity.toInt()}", fontSize = 11.sp, color = TextSecondary)
                                batch.expiryDate?.let { Spacer(modifier = Modifier.width(8.dp)); Text("Exp: ${dateFormat.format(Date(it))}", fontSize = 11.sp, color = if (isExpired) RedAccent else if (isExpiringSoon) Color(0xFFF79F1F) else TextSecondary) }
                            }
                        }
                        IconButton(onClick = { showDeleteConfirm = batch }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            item { if (batches.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.QrCode2, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No batches yet", fontSize = 14.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
