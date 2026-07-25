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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFixedAssetScreen(navController: NavController) {
    var assetName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Electronics") }
    var purchasePrice by remember { mutableStateOf("") }
    var salvageValue by remember { mutableStateOf("") }
    var usefulLife by remember { mutableStateOf("") }
    var depreciationMethod by remember { mutableStateOf("SLM") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var purchaseDate by remember { mutableStateOf("Select Date") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Electronics", "Furniture", "Vehicles", "Appliances", "Machinery", "Building", "Land", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Fixed Asset", fontWeight = FontWeight.Bold) },
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
                .background(VyaparBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Asset Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                    OutlinedTextField(
                        value = assetName,
                        onValueChange = { assetName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Asset Name") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )

                    Box {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Category") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showCategoryDropdown = true }) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                        )
                        DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = { selectedCategory = cat; showCategoryDropdown = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Purchase Date") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.clickable { showDatePicker = true }) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )

                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Purchase Price (\u20B9)") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )

                    OutlinedTextField(
                        value = salvageValue,
                        onValueChange = { salvageValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Salvage Value (\u20B9)") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )

                    OutlinedTextField(
                        value = usefulLife,
                        onValueChange = { usefulLife = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Useful Life (Years)") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Depreciation Method", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = depreciationMethod == "SLM",
                            onClick = { depreciationMethod = "SLM" },
                            label = { Text("SLM (Straight Line)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparLightBlue,
                                selectedLabelColor = Primary
                            )
                        )
                        FilterChip(
                            selected = depreciationMethod == "WDV",
                            onClick = { depreciationMethod = "WDV" },
                            label = { Text("WDV (Written Down)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparLightBlue,
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Asset", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
