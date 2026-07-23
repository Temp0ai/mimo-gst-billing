package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToSaleScreen(
    navController: NavController,
    invoiceType: String = "sales",
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val isPurchase = invoiceType == "purchase"
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var taxIncluded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("Pcs") }
    val units = listOf("Pcs", "Kg", "Gm", "Ltr", "Mtr", "Sqm", "Box", "Pair", "Set", "Doz", "Btl", "Bag", "Roll", "Bundle", "Pack", "Nos")
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = Color(0xFFD0D0D0),
        focusedLabelColor = Primary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = Primary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Items to ${if (isPurchase) "Purchase" else "Sale"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextSecondary
                )
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val name = itemName.trim()
                            val price = rate.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && price > 0) {
                                viewModel.addItemDirectWithDetails(name, price, 18.0, "", selectedUnit, quantity.toDoubleOrNull() ?: 1.0)
                                itemName = ""
                                rate = ""
                                quantity = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0D0D0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        enabled = itemName.isNotBlank() && (rate.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save & New", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            val name = itemName.trim()
                            val price = rate.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && price > 0) {
                                viewModel.addItemDirectWithDetails(name, price, 18.0, "", selectedUnit, quantity.toDoubleOrNull() ?: 1.0)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        enabled = itemName.isNotBlank() && (rate.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F6F6))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Item Name") },
                placeholder = { Text("e.g. Chocolate Cake") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = fieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.weight(0.4f),
                    label = { Text("Quantity", maxLines = 1) },
                    placeholder = { Text("1") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.weight(0.6f)
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Unit") },
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = { selectedUnit = unit; unitExpanded = false }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    modifier = Modifier.weight(0.55f),
                    label = { Text("Rate (Price/Unit)", maxLines = 1) },
                    placeholder = { Text("0.00") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors
                )

                Column(
                    modifier = Modifier.weight(0.45f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        FilterChip(
                            selected = !taxIncluded,
                            onClick = { taxIncluded = false },
                            label = { Text("Without Tax", fontSize = 11.sp, maxLines = 1) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.12f),
                                containerColor = Color.White,
                                selectedLabelColor = Primary,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFFD0D0D0),
                                selectedBorderColor = Primary.copy(alpha = 0.4f),
                                enabled = true,
                                selected = !taxIncluded
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = taxIncluded,
                            onClick = { taxIncluded = true },
                            label = { Text("With Tax", fontSize = 11.sp, maxLines = 1) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.12f),
                                containerColor = Color.White,
                                selectedLabelColor = Primary,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFFD0D0D0),
                                selectedBorderColor = Primary.copy(alpha = 0.4f),
                                enabled = true,
                                selected = taxIncluded
                            )
                        )
                    }
                }
            }
        }
    }
}
