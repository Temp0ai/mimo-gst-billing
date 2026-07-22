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
import com.mimo.gstbilling.ui.viewmodel.DiscountConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountConfigScreen(navController: NavController, viewModel: DiscountConfigViewModel = hiltViewModel()) {
    val discounts by viewModel.discounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Item Level", "Party Level", "Bill Level")
    val tabTypes = listOf("item_level", "party_level", "bill_level")

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var valueType by remember { mutableStateOf("percentage") }
        var showValueTypeDropdown by remember { mutableStateOf(false) }

        AlertDialog(onDismissRequest = { showAddDialog = false },
            title = { Text("Add Discount", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Discount Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Value *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Box {
                        OutlinedTextField(value = valueType.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Type") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showValueTypeDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showValueTypeDropdown, onDismissRequest = { showValueTypeDropdown = false }) {
                            DropdownMenuItem(text = { Text("Percentage (%)") }, onClick = { valueType = "percentage"; showValueTypeDropdown = false })
                            DropdownMenuItem(text = { Text("Flat Amount") }, onClick = { valueType = "flat"; showValueTypeDropdown = false })
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val v = value.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && v > 0) {
                    viewModel.addDiscount(name, tabTypes[selectedTab], v, valueType, null, null)
                    showAddDialog = false
                }
            }, enabled = name.isNotBlank() && (value.toDoubleOrNull() ?: 0.0) > 0) { Text("Add", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Discount Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = RedAccent, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add Discount") } }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, tab ->
                    FilterChip(selected = selectedTab == index, onClick = { selectedTab = index }, label = { Text(tab, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                }
            }
            val filteredDiscounts = discounts.filter { it.type == tabTypes[selectedTab] }
            LazyColumn {
                items(filteredDiscounts) { discount ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(discount.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("${discount.value}${if (discount.valueType == "percentage") "%" else " Flat"}", fontSize = 12.sp, color = Primary)
                            }
                            Switch(checked = discount.isActive, onCheckedChange = { viewModel.toggleActive(discount) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBalance))
                            IconButton(onClick = { viewModel.deleteDiscount(discount) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
