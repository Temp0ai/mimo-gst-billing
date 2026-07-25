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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReportBuilderScreen(
    navController: NavController
) {
    var reportName by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val availableFields = listOf("Date", "Party Name", "Invoice Number", "Amount", "Tax", "Discount", "Payment Status", "Item Name", "Quantity", "Unit Price", "HSN Code", "GST Rate")
    val selectedFields = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Report Builder", fontWeight = FontWeight.Bold) },
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
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Details", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reportName, onValueChange = { reportName = it },
                        label = { Text("Report Name") },
                        placeholder = { Text("My Custom Report") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider),
                        singleLine = true
                    )
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Fields", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedField, onValueChange = {}, readOnly = true,
                                label = { Text("Select Field") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                availableFields.filter { it !in selectedFields }.forEach { field ->
                                    DropdownMenuItem(text = { Text(field) }, onClick = { selectedField = field; expanded = false })
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (selectedField.isNotBlank() && selectedField !in selectedFields) {
                                    selectedFields.add(selectedField); selectedField = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Icon(Icons.Filled.Add, contentDescription = null) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Selected Fields (${selectedFields.size})", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedFields.isEmpty()) {
                        Text("No fields selected. Add fields from the dropdown above.", color = TextSecondary, fontSize = 13.sp)
                    }
                    selectedFields.forEachIndexed { index, field ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.DragIndicator, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(32.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(field, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            IconButton(onClick = { selectedFields.removeAt(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Preview", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Report: ${reportName.ifBlank { "Untitled" }}", fontSize = 13.sp, color = TextSecondary)
                    Text("Columns: ${selectedFields.size}", fontSize = 13.sp, color = TextSecondary)
                    if (selectedFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedFields.joinToString(" | "), fontSize = 12.sp, color = Primary)
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = reportName.isNotBlank() && selectedFields.isNotEmpty()
            ) {
                Icon(Icons.Filled.Save, contentDescription = null); Spacer(modifier = Modifier.width(8.dp))
                Text("Save Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
