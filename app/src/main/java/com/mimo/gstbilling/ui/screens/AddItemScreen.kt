package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    var itemName by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var gstRate by remember { mutableStateOf("18") }
    var stockQty by remember { mutableStateOf("0") }
    var unit by remember { mutableStateOf("NOS") }
    var isService by remember { mutableStateOf(false) }
    var showGstMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    val gstOptions = listOf("0", "5", "12", "18", "28")
    val unitOptions = listOf("NOS", "PCS", "KG", "GM", "M", "FT", "L", "ML", "BOX", "SET", "PAIR")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add New Item", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary), border = ButtonDefaults.outlinedButtonBorder) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(onClick = { if (itemName.isNotBlank() && salePrice.isNotBlank()) { viewModel.createItem(itemName, hsnCode.ifBlank { null }, salePrice.toDouble(), purchasePrice.toDoubleOrNull(), gstRate.toInt(), stockQty.toDouble(), unit, isService); navController.popBackStack() } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Inventory, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = hsnCode, onValueChange = { hsnCode = it }, label = { Text("HSN Code", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, tint = Primary) })

                    ExposedDropdownMenuBox(expanded = showGstMenu, onExpandedChange = { showGstMenu = it }) {
                        OutlinedTextField(value = "$gstRate%", onValueChange = {}, readOnly = true, label = { Text("GST Rate", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGstMenu) }, leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, tint = Primary) })
                        ExposedDropdownMenu(expanded = showGstMenu, onDismissRequest = { showGstMenu = false }) {
                            gstOptions.forEach { rate -> DropdownMenuItem(text = { Text("$rate%") }, onClick = { gstRate = rate; showGstMenu = false }) }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price *", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                        OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Purchase Price", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                    }

                    OutlinedTextField(value = stockQty, onValueChange = { stockQty = it }, label = { Text("Opening Stock", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Store, contentDescription = null, tint = Primary) })

                    ExposedDropdownMenuBox(expanded = showUnitMenu, onExpandedChange = { showUnitMenu = it }) {
                        OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) }, leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = null, tint = Primary) })
                        ExposedDropdownMenu(expanded = showUnitMenu, onDismissRequest = { showUnitMenu = false }) {
                            unitOptions.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; showUnitMenu = false }) }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Service Item", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = isService, onCheckedChange = { isService = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                }
            }
        }
    }
}
