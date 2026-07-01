package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    navController: NavController,
    viewModel: ItemViewModel = hiltViewModel()
) {
    var itemName by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var gstRate by remember { mutableIntStateOf(0) }
    var unit by remember { mutableStateOf("Piece") }
    var stock by remember { mutableStateOf("") }
    var gstExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var isService by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val gstRates = listOf("0%", "5%", "12%", "18%", "28%")
    val gstValues = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
    val units = listOf("Piece", "Kg", "Liter", "Meter", "Box", "Dozen", "Pcs", "Nos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Item", fontWeight = FontWeight.Bold)
                },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Item Name *",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter item name") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text(
                            text = "HSN Code",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = hsnCode,
                            onValueChange = { hsnCode = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter HSN code") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sale Price",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                OutlinedTextField(
                                    value = salePrice,
                                    onValueChange = { salePrice = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("0.00") },
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Purchase Price",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                OutlinedTextField(
                                    value = purchasePrice,
                                    onValueChange = { purchasePrice = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("0.00") },
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GST Rate",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                ExposedDropdownMenuBox(
                                    expanded = gstExpanded,
                                    onExpandedChange = { gstExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = gstRates[gstRate],
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstExpanded)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = gstExpanded,
                                        onDismissRequest = { gstExpanded = false }
                                    ) {
                                        gstRates.forEachIndexed { index, rate ->
                                            DropdownMenuItem(
                                                text = { Text(rate) },
                                                onClick = {
                                                    gstRate = index
                                                    gstExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unit",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                ExposedDropdownMenuBox(
                                    expanded = unitExpanded,
                                    onExpandedChange = { unitExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = unit,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = unitExpanded,
                                        onDismissRequest = { unitExpanded = false }
                                    ) {
                                        units.forEach { unitOption ->
                                            DropdownMenuItem(
                                                text = { Text(unitOption) },
                                                onClick = {
                                                    unit = unitOption
                                                    unitExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Stock Quantity",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter stock quantity") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.addItem(
                                name = itemName,
                                hsnCode = hsnCode,
                                description = null,
                                salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                gstRate = gstValues[gstRate],
                                unit = unit,
                                stockQuantity = stock.toDoubleOrNull() ?: 0.0,
                                isService = isService
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                    shape = RoundedCornerShape(10.dp),
                    enabled = itemName.isNotEmpty()
                ) {
                    Text(
                        text = "Save Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
