package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class BulkItem(
    val id: Long,
    val name: String,
    val type: String,
    val price: Double,
    val stock: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBulkOperationsScreen(
    navController: NavController
) {
    var selectAll by remember { mutableStateOf(false) }
    var selectedOperation by remember { mutableStateOf("Update Price") }
    var expanded by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val operations = listOf("Update Price", "Update Tax", "Change Category", "Adjust Stock", "Delete")

    val items = remember {
        mutableStateListOf(
            BulkItem(1, "Laptop HP 15s", "Electronics", 45000.0, 25.0),
            BulkItem(2, "Office Chair", "Furniture", 8500.0, 15.0),
            BulkItem(3, "LED Bulb 9W", "Lighting", 120.0, 200.0),
            BulkItem(4, "Cement ACC 50kg", "Building Material", 380.0, 500.0),
            BulkItem(5, "Printer Canon G3010", "Electronics", 13500.0, 8.0),
            BulkItem(6, "Mouse Logitech", "Electronics", 450.0, 50.0),
            BulkItem(7, "Pipe SS 1 inch", "Plumbing", 450.0, 100.0),
            BulkItem(8, "Consulting Service", "Services", 5000.0, 0.0)
        )
    }

    val checkedItems = remember { mutableStateMapOf<Long, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk Operations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectAll,
                            onCheckedChange = { checked ->
                                selectAll = checked
                                items.forEach { checkedItems[it.id] = checked }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Text("Select All (${checkedItems.count { it.value }} of ${items.size})", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    HorizontalDivider(color = Divider)
                    items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = checkedItems[item.id] == true,
                                onCheckedChange = { checked -> checkedItems[item.id] = checked },
                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text("${item.type} | ₹${String.format("%.0f", item.price)} | Stock: ${item.stock.toInt()}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Operation", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedOperation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Operation") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            operations.forEach { op ->
                                DropdownMenuItem(
                                    text = { Text(op) },
                                    onClick = { selectedOperation = op; expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            if (isProcessing) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing bulk operation...", color = TextPrimary)
                    }
                }
            }

            Button(
                onClick = { isProcessing = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                enabled = checkedItems.any { it.value } && !isProcessing
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Bulk Action", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
